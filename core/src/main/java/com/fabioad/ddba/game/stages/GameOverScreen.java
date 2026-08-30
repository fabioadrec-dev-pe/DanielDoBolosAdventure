package com.fabioad.ddba.game.stages;

import com.badlogic.gdx.graphics.Color;
import com.fabioad.ddba.engine.assets.AssetPaths;
import com.fabioad.ddba.engine.core.GameConfig;
import com.fabioad.ddba.engine.input.GameInput;
import com.fabioad.ddba.engine.ui.TextUtil;
import com.fabioad.ddba.game.core.BaseGameScreen;
import com.fabioad.ddba.game.core.DanielGame;

/**
 * GameOverScreen
 * =============================================================================
 * OBJETIVO DA CLASSE:
 *   Exibir "GAME OVER" com a pontuacao final e aguardar START para voltar ao menu.
 *   Toca a musica/efeito de derrota.
 *
 * PORTABILIDADE:
 *   - SNES: tela simples de tiles + texto; o retorno ao menu reinicia o estado do
 *     jogo na WRAM.
 */
public final class GameOverScreen extends BaseGameScreen {

    private float time;

    public GameOverScreen(DanielGame game) {
        super(game);
    }

    @Override
    public void show() {
        ctx.render.resetWorldCamera();
        ctx.audio.playMusic(AssetPaths.MUSIC_GAMEOVER, false);
    }

    @Override
    protected void update(float dt) {
        time += dt;
        // Pequeno atraso antes de aceitar input (evita pular sem querer).
        if (time > 0.8f && ctx.input.isPressed(GameInput.Action.START)) {
            ctx.audio.playSfx("select");
            game.changeScreen(new MenuScreen(game));
        }
    }

    @Override
    protected void draw() {
        // HUD projection: coordenadas 0..256x224 (a camera do mundo pode estar scrollada).
        ctx.render.applyHudProjection();

        float w = GameConfig.VIRTUAL_WIDTH, h = GameConfig.VIRTUAL_HEIGHT;
        ctx.render.fillRect(0, 0, w, h, Color.BLACK);
        TextUtil.drawCentered(ctx.render.getBatch(), ctx.assets.getFont(),
                "GAME OVER", w / 2f, h * 0.6f, 1.2f, Color.RED);
        TextUtil.drawCentered(ctx.render.getBatch(), ctx.assets.getFont(),
                "PONTOS: " + session.getScore(), w / 2f, h * 0.42f, 0.5f, Color.WHITE);
        if (time > 0.8f && ((int) (time * 2)) % 2 == 0) {
            TextUtil.drawCentered(ctx.render.getBatch(), ctx.assets.getFont(),
                    "PRESS START", w / 2f, h * 0.22f, 0.5f, Color.LIGHT_GRAY);
        }
    }
}
