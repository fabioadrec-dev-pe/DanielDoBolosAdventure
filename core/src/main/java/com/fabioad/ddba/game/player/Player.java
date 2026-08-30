package com.fabioad.ddba.game.player;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.fabioad.ddba.engine.collision.TileCollisionResolver;
import com.fabioad.ddba.engine.core.GameConfig;
import com.fabioad.ddba.engine.core.GameContext;
import com.fabioad.ddba.engine.input.GameInput;
import com.fabioad.ddba.game.entities.Entity;
import com.fabioad.ddba.game.maps.TileMap;

/**
 * Player (Daniel do Bolo)
 * =============================================================================
 * OBJETIVO DA CLASSE:
 *   Implementar o HEROI: leitura de controle, fisica de plataforma (aceleracao,
 *   atrito, gravidade, PULO VARIAVEL), colisao por tiles, maquina de estados de
 *   animacao e as reacoes a dano/morte/vitoria.
 *
 * ALGORITMOS-CHAVE:
 *   - Movimento horizontal com ACELERACAO/ATRITO: em vez de setar a velocidade
 *     direto, aproximamos a velocidade-alvo por aceleracao, e freamos por atrito
 *     quando nao ha input. Isso da o "peso"/inercia classico de 16 bits.
 *   - PULO VARIAVEL: enquanto o botao de pulo e mantido e o heroi sobe, aplica-se
 *     uma gravidade menor; ao soltar, a gravidade cheia encurta o pulo. Assim o
 *     jogador controla a ALTURA do pulo pela duracao do toque.
 *   - COLISAO por eixo separado via TileCollisionResolver (encosta e zera eixo).
 *
 * DECISOES DE ARQUITETURA:
 *   - A caixa de colisao (16x24) e menor que o sprite (24x32); o desenho e
 *     centralizado sobre a caixa. Separar "hitbox" de "arte" e boa pratica.
 *   - O Player usa servicos do engine (input/assets/audio) via GameContext, mas
 *     nao conhece as OUTRAS telas nem a sessao: baixo acoplamento.
 *
 * PORTABILIDADE:
 *   - SNES: velocidades/aceleracoes viram ponto-fixo 8.8; a gravidade e um dv por
 *     quadro. O "pulo variavel" e identico e classico no 65816.
 */
public final class Player extends Entity {

    // Dimensoes de colisao (hitbox) e do quadro de sprite (arte).
    private static final float BOX_W = 16f;
    private static final float BOX_H = 24f;
    private static final int FRAME_W = 24;
    private static final int FRAME_H = 32;

    private final GameContext ctx;
    private final TileCollisionResolver resolver = new TileCollisionResolver();

    private PlayerState state = PlayerState.IDLE;
    private boolean onGround;
    private boolean jumpHeld;

    /** Invencibilidade temporaria (piscar) apos tomar dano. */
    private float invincibleTimer;

    /** Cronometro do estado de morte (para a tela saber quando respawnar). */
    private float deadTimer;

    /** Flag de vitoria (pose comemorativa, sem controle). */
    private boolean victory;

    public Player(GameContext ctx, float x, float y) {
        super(x, y, BOX_W, BOX_H);
        this.ctx = ctx;
    }

    /** Reposiciona e zera o estado (respawn / inicio de fase). */
    public void reset(float x, float y) {
        box.set(x, y, BOX_W, BOX_H);
        vx = vy = 0;
        onGround = false;
        state = PlayerState.IDLE;
        stateTime = 0;
        invincibleTimer = 0;
        deadTimer = 0;
        victory = false;
        alive = true;
        facingRight = true;
    }

    public boolean isDead() {
        return state == PlayerState.DEAD;
    }

    public boolean isDeathAnimationFinished() {
        return state == PlayerState.DEAD && deadTimer > 1.6f;
    }

    public void setVictory() {
        victory = true;
        state = PlayerState.VICTORY;
        vx = 0;
    }

    /**
     * Sofre dano de um inimigo (sem ser a morte definitiva).
     * Ativa invencibilidade + piscada por {@link GameConfig#PLAYER_INVINCIBLE_DURATION}.
     * Retorna true se o dano foi aplicado; false se ja estava invencivel/morto.
     * A PlayScreen deve chamar loseLife() e, se era a ultima vida, {@link #kill()}.
     */
    public boolean hurt() {
        if (state == PlayerState.DEAD || invincibleTimer > 0) return false;

        invincibleTimer = GameConfig.PLAYER_INVINCIBLE_DURATION;
        state = PlayerState.HURT;
        stateTime = 0;
        // Knockback leve (empurra na direcao oposta a que encara).
        vx = facingRight ? -90f : 90f;
        vy = GameConfig.PLAYER_JUMP_VELOCITY * 0.45f;
        onGround = false;
        ctx.audio.playSfx("hurt");
        return true;
    }

    /** Concede invencibilidade + piscada (ex.: apos respawn por buraco). */
    public void grantInvincibility() {
        if (state == PlayerState.DEAD) return;
        invincibleTimer = GameConfig.PLAYER_INVINCIBLE_DURATION;
    }

    public boolean isInvincible() {
        return invincibleTimer > 0;
    }

    /** Morte definitiva (animacao de queda): buraco, espinho fatal, ultima vida. */
    public void kill() {
        if (state == PlayerState.DEAD) return;
        state = PlayerState.DEAD;
        deadTimer = 0;
        invincibleTimer = 0; // para a animacao de morte nao "piscar"
        vx = 0;
        vy = GameConfig.PLAYER_JUMP_VELOCITY * 0.7f; // pulinho de morte
        ctx.audio.playSfx("hurt");
    }

    /** Salto ao pisar num inimigo (bounce). */
    public void bounce() {
        vy = GameConfig.PLAYER_JUMP_VELOCITY * 0.6f;
        onGround = false;
    }

    @Override
    public void update(float dt) {
        stateTime += dt;
        if (invincibleTimer > 0) invincibleTimer -= dt;

        if (state == PlayerState.DEAD) {
            updateDead(dt);
            return;
        }
        if (victory) {
            // Pose de vitoria: aplica gravidade so para assentar no chao.
            applyGravity(dt);
            moveAndCollide(dt);
            return;
        }

        handleHorizontal(dt);
        handleJump(dt);
        applyGravity(dt);
        moveAndCollide(dt);
        updateState();

        // Queda em buraco (abaixo do mapa) => morte.
        if (box.y < -FRAME_H) {
            kill();
        }
    }

    private void updateDead(float dt) {
        deadTimer += dt;
        // Cai livremente (animacao de morte) sem colidir com tiles.
        vy -= GameConfig.GRAVITY * dt;
        box.y += vy * dt;
    }

    /** Movimento horizontal com aceleracao (input) e atrito (sem input). */
    private void handleHorizontal(float dt) {
        GameInput in = ctx.input;
        boolean left = in.isDown(GameInput.Action.LEFT);
        boolean right = in.isDown(GameInput.Action.RIGHT);
        boolean running = in.isDown(GameInput.Action.RUN);
        boolean crouching = onGround && in.isDown(GameInput.Action.DOWN);

        // Abaixado no chao: freia e nao anda (classico).
        if (crouching) {
            vx = MathUtils.lerp(vx, 0, Math.min(1f, GameConfig.PLAYER_FRICTION * dt / 100f));
            if (Math.abs(vx) < 5f) vx = 0;
            return;
        }

        float maxSpeed = running ? GameConfig.PLAYER_RUN_SPEED : GameConfig.PLAYER_WALK_SPEED;

        if (left && !right) {
            vx -= GameConfig.PLAYER_ACCEL * dt;
            facingRight = false;
        } else if (right && !left) {
            vx += GameConfig.PLAYER_ACCEL * dt;
            facingRight = true;
        } else {
            // Sem input: aplica atrito ate parar.
            float fr = GameConfig.PLAYER_FRICTION * dt;
            if (vx > 0) vx = Math.max(0, vx - fr);
            else if (vx < 0) vx = Math.min(0, vx + fr);
        }
        vx = MathUtils.clamp(vx, -maxSpeed, maxSpeed);
    }

    /** Pulo com altura variavel. */
    private void handleJump(float dt) {
        GameInput in = ctx.input;
        boolean jumpDown = in.isDown(GameInput.Action.JUMP);

        if (in.isPressed(GameInput.Action.JUMP) && onGround) {
            vy = GameConfig.PLAYER_JUMP_VELOCITY;
            onGround = false;
            jumpHeld = true;
            ctx.audio.playSfx("jump");
        }
        // Ao soltar cedo enquanto sobe, corta o impulso (encurta o pulo).
        if (!jumpDown && jumpHeld && vy > 0) {
            vy *= 0.5f;
            jumpHeld = false;
        }
        if (!jumpDown) jumpHeld = false;
    }

    private void applyGravity(float dt) {
        // Gravidade reduzida enquanto sobe segurando o botao (pulo variavel).
        boolean holdingUp = ctx.input.isDown(GameInput.Action.JUMP) && vy > 0;
        float g = holdingUp ? GameConfig.PLAYER_JUMP_HOLD_GRAVITY : GameConfig.GRAVITY;
        vy -= g * dt;
        if (vy < -GameConfig.MAX_FALL_SPEED) vy = -GameConfig.MAX_FALL_SPEED;
    }

    private TileMap map;

    /** Define o mapa atual (chamado pela PlayScreen ao carregar a fase). */
    public void setMap(TileMap map) {
        this.map = map;
    }

    private void moveAndCollide(float dt) {
        if (map == null) return;
        float dx = vx * dt;
        float dy = vy * dt;
        TileCollisionResolver.CollisionResult r = resolver.move(box, dx, dy, map);
        if (r.hitLeft || r.hitRight) vx = 0;
        if (r.hitCeiling) vy = 0;
        onGround = r.onGround;
        if (onGround && vy < 0) vy = 0;

        // Espinho: checa o tile sob os pes (ignorado enquanto invencivel).
        if (invincibleTimer <= 0f) {
            int ts = map.getTileSize();
            int footRow = (int) Math.floor((box.y - 1) / ts);
            int col = (int) Math.floor(box.centerX() / ts);
            if (map.isHazard(col, footRow) || map.isHazard(col, (int) Math.floor(box.y / ts))) {
                kill();
            }
        }
    }

    /** Escolhe o estado (animacao) a partir do movimento atual. */
    private void updateState() {
        GameInput in = ctx.input;
        if (!onGround) {
            state = (vy > 0) ? PlayerState.JUMP : PlayerState.FALL;
            return;
        }
        if (in.isDown(GameInput.Action.DOWN)) {
            state = PlayerState.CROUCH;
            return;
        }
        if (in.isDown(GameInput.Action.UP) && Math.abs(vx) < 5f) {
            state = PlayerState.LOOK_UP;
            return;
        }
        if (Math.abs(vx) > GameConfig.PLAYER_WALK_SPEED + 1f) {
            state = PlayerState.RUN;
        } else if (Math.abs(vx) > 5f) {
            state = PlayerState.WALK;
        } else {
            state = PlayerState.IDLE;
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        // Piscar durante invencibilidade (esconde em quadros alternados).
        if (invincibleTimer > 0 && ((int) (invincibleTimer * 20) % 2 == 0)) {
            return;
        }

        Animation<TextureRegion> anim = currentAnimation();
        if (anim == null) return;
        boolean looping = state == PlayerState.IDLE || state == PlayerState.WALK
                || state == PlayerState.RUN || state == PlayerState.VICTORY;
        TextureRegion frame = anim.getKeyFrame(stateTime, looping);

        // Desenho centralizado sobre a hitbox (arte 24x32, hitbox 16x24).
        float drawX = box.x - (FRAME_W - BOX_W) / 2f;
        float drawY = box.y;
        float w = FRAME_W;
        if (!facingRight) {
            // Espelha horizontalmente desenhando com largura negativa.
            batch.draw(frame, drawX + FRAME_W, drawY, -w, FRAME_H);
        } else {
            batch.draw(frame, drawX, drawY, w, FRAME_H);
        }
    }

    private Animation<TextureRegion> currentAnimation() {
        switch (state) {
            case WALK: return ctx.assets.playerWalk;
            case RUN: return ctx.assets.playerRun;
            case JUMP: return ctx.assets.playerJump;
            case FALL: return ctx.assets.playerFall;
            case CROUCH: return ctx.assets.playerCrouch;
            case LOOK_UP: return ctx.assets.playerLookUp;
            case HURT: return ctx.assets.playerHurt;
            case DEAD: return ctx.assets.playerDead;
            case VICTORY: return ctx.assets.playerVictory;
            case IDLE:
            default: return ctx.assets.playerIdle;
        }
    }

    public PlayerState getState() {
        return state;
    }
}
