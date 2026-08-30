package com.fabioad.ddba.game.stages;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.fabioad.ddba.engine.assets.AssetPaths;
import com.fabioad.ddba.engine.camera.SideScrollerCamera;
import com.fabioad.ddba.engine.core.GameConfig;
import com.fabioad.ddba.engine.input.GameInput;
import com.fabioad.ddba.engine.ui.TextUtil;
import com.fabioad.ddba.game.core.BaseGameScreen;
import com.fabioad.ddba.game.core.DanielGame;
import com.fabioad.ddba.game.enemies.Enemy;
import com.fabioad.ddba.game.enemies.EnemyFactory;
import com.fabioad.ddba.game.entities.Coin;
import com.fabioad.ddba.game.hud.Hud;
import com.fabioad.ddba.game.maps.StageData;
import com.fabioad.ddba.game.maps.StageFactory;
import com.fabioad.ddba.game.maps.TileMap;
import com.fabioad.ddba.game.player.Player;

/**
 * PlayScreen (Tela de Jogo)
 * =============================================================================
 * OBJETIVO DA CLASSE:
 *   Orquestrar UMA fase: carregar dados, criar entidades, rodar a simulacao
 *   (jogador + inimigos + itens), resolver interacoes (pisao, dano, moedas,
 *   checkpoints, objetivo), atualizar a camera lateral, desenhar mundo + HUD e
 *   gerenciar as transicoes (morte -> respawn/game over; objetivo -> proxima fase).
 *
 * DECISOES DE ARQUITETURA:
 *   - A tela e o "maestro": conhece as regras de fase, mas delega fisica/colisao
 *     ao engine e comportamento ao Player/Enemy. Baixo acoplamento, alta coesao.
 *   - Maquina de estados da tela: PLAYING -> DYING -> (respawn|game over) e
 *     PLAYING -> CLEAR -> (proxima fase|vitoria final).
 *
 * ALGORITMO DE INTERACAO JOGADOR x INIMIGO (classico 16 bits):
 *   Se o jogador esta CAINDO e seus pes estao acima do centro do inimigo -> PISAO
 *   (derrota o inimigo e quica); caso contrario -> DANO no jogador.
 *
 * PORTABILIDADE:
 *   - SNES: este seria o "main loop de gameplay" chamado a cada VBlank, iterando
 *     a lista de objetos ativos e checando colisoes por AABB/atributos de tile.
 */
public final class PlayScreen extends BaseGameScreen {

    private enum PlayState { PLAYING, DYING, CLEAR }

    private StageData stage;
    private TileMap map;
    private Player player;
    private final Array<Enemy> enemies = new Array<>();
    private final Array<Coin> coins = new Array<>();
    private SideScrollerCamera camera;
    private Hud hud;

    private PlayState state = PlayState.PLAYING;
    private boolean paused;
    private float transitionTimer;

    // Ponto de respawn (ultimo checkpoint alcancado).
    private float respawnX, respawnY;
    private int reachedCheckpoints;

    public PlayScreen(DanielGame game) {
        super(game);
    }

    @Override
    public void show() {
        loadStage();
        // Musica: chefe na ultima fase, tema normal nas demais.
        ctx.audio.playMusic(stage.bossStage ? AssetPaths.MUSIC_BOSS : AssetPaths.MUSIC_STAGE, true);
    }

    /** Constroi a fase atual (mapa + entidades) e posiciona a camera. */
    private void loadStage() {
        stage = StageFactory.build(session.getStageIndex());
        map = stage.map;

        player = new Player(ctx, stage.playerStartX, stage.playerStartY);
        player.setMap(map);
        respawnX = stage.playerStartX;
        respawnY = stage.playerStartY;
        reachedCheckpoints = 0;

        enemies.clear();
        for (StageData.EnemySpawn s : stage.enemies) {
            enemies.add(EnemyFactory.create(s.type, ctx.assets, map, s.x, s.y));
        }

        coins.clear();
        for (StageData.Point p : stage.coins) {
            coins.add(new Coin(ctx.assets.coin, p.x, p.y));
        }

        camera = new SideScrollerCamera(ctx.render.getWorldCamera());
        camera.setWorldBounds(map.getWidthPixels(), map.getHeightPixels());
        camera.snapTo(player.getCenterX(), player.getCenterY());

        hud = new Hud(ctx.assets.getFont());
        state = PlayState.PLAYING;
        session.setStageTime(300f);
    }

    @Override
    protected void update(float dt) {
        // Pausa (START). Voltar ao menu (BACK/ESC).
        if (ctx.input.isPressed(GameInput.Action.START)) {
            paused = !paused;
            ctx.audio.playSfx("menu");
        }
        if (ctx.input.isPressed(GameInput.Action.BACK)) {
            game.changeScreen(new MenuScreen(game));
            return;
        }
        if (paused) return;

        switch (state) {
            case PLAYING: updatePlaying(dt); break;
            case DYING: updateDying(dt); break;
            case CLEAR: updateClear(dt); break;
        }

        camera.follow(player.getCenterX(), player.getCenterY(), dt);
    }

    private void updatePlaying(float dt) {
        // Tempo da fase.
        session.tickTime(dt);
        if (session.getStageTime() <= 0f) {
            player.kill();
        }

        player.update(dt);
        for (Enemy e : enemies) e.update(dt);
        for (Coin c : coins) c.update(dt);

        handleCoinPickup();
        handleEnemyInteractions();
        handleCheckpoints();

        // Objetivo alcancado?
        if (player.getX() >= stage.goalX) {
            startStageClear();
        }

        // Morte iniciada dentro deste passo?
        if (player.isDead()) {
            state = PlayState.DYING;
        }
    }

    private void updateDying(float dt) {
        player.update(dt);
        if (player.isDeathAnimationFinished()) {
            if (session.loseLife()) {
                respawnPlayer();
            } else {
                ctx.audio.playSfx("gameover");
                game.changeScreen(new GameOverScreen(game));
            }
        }
    }

    private void updateClear(float dt) {
        player.update(dt);
        for (Coin c : coins) c.update(dt);
        transitionTimer += dt;
        if (transitionTimer >= 3f) {
            session.nextStage();
            if (session.isLastStageCleared()) {
                game.changeScreen(new EndingStoryScreen(game));
            } else {
                game.changeScreen(new PlayScreen(game));
            }
        }
    }

    private void startStageClear() {
        if (state == PlayState.CLEAR) return;
        state = PlayState.CLEAR;
        transitionTimer = 0;
        player.setVictory();
        ctx.audio.playSfx("victory");
        // Bonus por tempo restante.
        session.addScore((long) session.getStageTime() * 10);
    }

    private void respawnPlayer() {
        player.reset(respawnX, respawnY);
        // Apos perder a vida (buraco/espinho), 3s de invencibilidade + piscada.
        player.grantInvincibility();
        camera.snapTo(player.getCenterX(), player.getCenterY());
        state = PlayState.PLAYING;
    }

    private void handleCoinPickup() {
        for (Coin c : coins) {
            if (c.alive && player.box.overlaps(c.box)) {
                c.alive = false;
                session.addCoin();
                ctx.audio.playSfx("coin");
            }
        }
    }

    private void handleEnemyInteractions() {
        if (player.isDead()) return;
        for (Enemy e : enemies) {
            if (!e.alive) continue;
            if (!player.box.overlaps(e.box)) continue;

            boolean falling = player.vy < 0;
            boolean feetAbove = player.box.y >= e.box.centerY();
            if (falling && feetAbove && e.isStompable()) {
                // PISAO: derrota (ou fere) o inimigo e quica.
                boolean defeated = e.onStomped();
                if (defeated) {
                    session.addScore(e.getPoints());
                    ctx.audio.playSfx(e.alive ? "menu" : "defeat");
                }
                player.bounce();
            } else {
                // DANO: perde 1 vida + invencibilidade/piscada 3s.
                // Ultima vida: morte definitiva → DYING → Game Over.
                if (session.getLives() > 1) {
                    if (player.hurt()) {
                        session.loseLife();
                    }
                } else if (!player.isInvincible() && !player.isDead()) {
                    player.kill();
                    state = PlayState.DYING;
                    return;
                }
            }
        }
    }

    private void handleCheckpoints() {
        for (int i = reachedCheckpoints; i < stage.checkpoints.size; i++) {
            StageData.Point cp = stage.checkpoints.get(i);
            if (player.getCenterX() >= cp.x) {
                respawnX = cp.x;
                respawnY = cp.y;
                reachedCheckpoints = i + 1;
                ctx.audio.playSfx("select");
            }
        }
    }

    @Override
    protected void draw() {
        SpriteBatch batch = ctx.render.getBatch();

        // 1) Fundo (identidade visual da fase) em projecao estatica.
        ctx.render.applyHudProjection();
        drawBackground(batch);

        // 2) Mundo (tiles + entidades) em projecao da camera.
        ctx.render.applyWorldProjection();
        float camLeft = ctx.render.getWorldCamera().position.x - GameConfig.VIRTUAL_WIDTH / 2f;
        float camBottom = ctx.render.getWorldCamera().position.y - GameConfig.VIRTUAL_HEIGHT / 2f;

        map.render(batch, ctx.assets.tiles, camLeft, camBottom);
        for (Coin c : coins) c.draw(batch);
        for (Enemy e : enemies) e.draw(batch);
        drawGoalFlag(batch);
        drawCastle(batch);
        player.draw(batch);

        // 3) HUD + overlays em projecao estatica.
        ctx.render.applyHudProjection();
        hud.draw(batch, session, stage.name);
        drawOverlays(batch);
    }

    private void drawBackground(SpriteBatch batch) {
        int idx = stage.backgroundIndex;
        TextureRegion bg = (idx >= 0 && idx < ctx.assets.backgrounds.length)
                ? ctx.assets.backgrounds[idx] : null;
        if (bg != null) {
            batch.setColor(Color.WHITE);
            batch.draw(bg, 0, 0, GameConfig.VIRTUAL_WIDTH, GameConfig.VIRTUAL_HEIGHT);
        } else {
            ctx.render.fillRect(0, 0, GameConfig.VIRTUAL_WIDTH, GameConfig.VIRTUAL_HEIGHT,
                    new Color(0.15f, 0.4f, 0.7f, 1f));
        }
    }

    /** Marca visual simples do objetivo (bandeira) no fim da fase. */
    private void drawGoalFlag(SpriteBatch batch) {
        ctx.render.fillRect(stage.goalX, stage.goalY, 3, 80, new Color(1f, 1f, 1f, 0.9f));
        ctx.render.fillRect(stage.goalX + 3, stage.goalY + 64, 16, 12, new Color(1f, 0.2f, 0.2f, 1f));
    }

    /**
     * Barraca/"castelo" DEPOIS da bandeira — visual inspirado em boteco de praia
     * (amarelo/vermelho, toldo branco, PITÚ). O herói completa a fase na bandeira;
     * a barraca e o marco visual de destinacao.
     */
    private void drawCastle(SpriteBatch batch) {
        if (ctx.assets.castle == null) return;
        float cw = 80f;
        float ch = 56f;
        // Apos a bandeira (~24 px a direita).
        float x = stage.goalX + 24f;
        float y = stage.goalY;
        batch.setColor(Color.WHITE);
        batch.draw(ctx.assets.castle, x, y, cw, ch);
    }

    private void drawOverlays(SpriteBatch batch) {
        float w = GameConfig.VIRTUAL_WIDTH, h = GameConfig.VIRTUAL_HEIGHT;
        if (paused) {
            ctx.render.fillRect(0, 0, w, h, new Color(0, 0, 0, 0.5f));
            TextUtil.drawCentered(batch, ctx.assets.getFont(), "PAUSA", w / 2f, h / 2f + 6, 0.5f, Color.WHITE);
            TextUtil.drawCentered(batch, ctx.assets.getFont(), "ENTER: continuar   ESC: menu",
                    w / 2f, h / 2f - 12, 0.5f, Color.LIGHT_GRAY);
        }
        if (state == PlayState.CLEAR) {
            TextUtil.drawCentered(batch, ctx.assets.getFont(), "FASE COMPLETA!", w / 2f, h * 0.6f, 0.5f, Color.GOLD);
        }
    }
}
