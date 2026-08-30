package com.fabioad.ddba.game.enemies;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.fabioad.ddba.engine.collision.TileCollisionResolver;
import com.fabioad.ddba.engine.core.GameConfig;
import com.fabioad.ddba.game.entities.Entity;
import com.fabioad.ddba.game.maps.TileMap;

/**
 * Enemy (base)
 * =============================================================================
 * OBJETIVO DA CLASSE:
 *   Base de TODOS os inimigos. Reune o que e comum (animacao, vida, pontuacao,
 *   colisao com tiles, gravidade) e helpers de IA (patrulha com virada em parede
 *   e em beira de plataforma). Cada subclasse implementa a sua IA em updateAI().
 *
 * DECISAO DE ARQUITETURA:
 *   "Template Method": update() (comum) chama updateAI() (especifico). Isso evita
 *   duplicar fisica/desenho em cada tipo e mantem cada IA isolada e testavel.
 *
 * PORTABILIDADE:
 *   - SNES: cada tipo seria uma rotina de update indexada por um byte "enemy_type".
 *     A "vida" e "pontos" ficariam em tabelas por tipo. A patrulha com deteccao de
 *     beira e um classico (checar o tile diagonal-a-frente-abaixo).
 */
public abstract class Enemy extends Entity {

    protected final Animation<TextureRegion> anim;
    protected final TileMap map;
    protected final TileCollisionResolver resolver = new TileCollisionResolver();

    protected int health = 1;
    protected int points = 200;
    /** Se true, morre ao ser pisado; se false, ignora o pisao (ex.: espinhoso). */
    protected boolean stompable = true;
    protected boolean onGround;

    protected Enemy(Animation<TextureRegion> anim, TileMap map,
                    float x, float y, float w, float h) {
        super(x, y, w, h);
        this.anim = anim;
        this.map = map;
        this.facingRight = false; // inimigos costumam comecar indo para a esquerda
    }

    /** IA especifica do tipo de inimigo. */
    protected abstract void updateAI(float dt);

    @Override
    public void update(float dt) {
        if (!alive) return;
        stateTime += dt;
        updateAI(dt);
    }

    /** Aplica gravidade e limita a velocidade de queda. */
    protected void applyGravity(float dt) {
        vy -= GameConfig.GRAVITY * dt;
        if (vy < -GameConfig.MAX_FALL_SPEED) vy = -GameConfig.MAX_FALL_SPEED;
    }

    /** Move colidindo com tiles (para inimigos "terrestres"). */
    protected void moveAndCollide(float dt) {
        TileCollisionResolver.CollisionResult r = resolver.move(box, vx * dt, vy * dt, map);
        onGround = r.onGround;
        if (onGround && vy < 0) vy = 0;
        if (r.hitCeiling) vy = 0;
        if (r.hitLeft) { vx = Math.abs(vx); facingRight = true; }
        if (r.hitRight) { vx = -Math.abs(vx); facingRight = false; }
    }

    /**
     * Patrulha terrestre: anda na direcao que encara; vira ao bater na parede ou
     * ao chegar na BEIRA de uma plataforma (para nao cair).
     */
    protected void patrol(float dt, float speed) {
        vx = facingRight ? speed : -speed;
        applyGravity(dt);
        moveAndCollide(dt);

        if (onGround) {
            int ts = map.getTileSize();
            // Coluna logo a frente, na altura dos pes.
            float aheadX = facingRight ? box.right() + 1 : box.x - 1;
            int col = (int) Math.floor(aheadX / ts);
            int rowBelow = (int) Math.floor((box.y - 1) / ts);
            if (!map.isSolid(col, rowBelow)) {
                // Sem chao a frente -> vira.
                facingRight = !facingRight;
            }
        }
    }

    /**
     * Chamado quando o jogador pisa neste inimigo.
     * @return true se o inimigo foi derrotado (jogador deve quicar).
     */
    public boolean onStomped() {
        if (!stompable) return false;
        health--;
        if (health <= 0) {
            alive = false;
        }
        return true;
    }

    public int getPoints() {
        return points;
    }

    public boolean isStompable() {
        return stompable;
    }

    @Override
    public void draw(SpriteBatch batch) {
        if (!alive || anim == null) return;
        TextureRegion frame = anim.getKeyFrame(stateTime, true);
        if (!facingRight) {
            batch.draw(frame, box.x + box.width, box.y, -box.width, box.height);
        } else {
            batch.draw(frame, box.x, box.y, box.width, box.height);
        }
    }
}
