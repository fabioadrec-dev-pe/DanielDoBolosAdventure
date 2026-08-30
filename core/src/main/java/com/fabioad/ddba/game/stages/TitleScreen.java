package com.fabioad.ddba.game.stages;

import com.badlogic.gdx.graphics.Color;
import com.fabioad.ddba.engine.assets.AssetPaths;
import com.fabioad.ddba.engine.core.GameConfig;
import com.fabioad.ddba.engine.input.GameInput;
import com.fabioad.ddba.engine.ui.TextUtil;
import com.fabioad.ddba.game.core.BaseGameScreen;
import com.fabioad.ddba.game.core.DanielGame;

/**
 * TitleScreen (Tela Inicial)
 * =============================================================================
 * OBJETIVO DA CLASSE:
 *   Tela-titulo com ANIMACAO: o titulo "Daniel do Bolo's Adventure" pulsa/oscila,
 *   ciclo de cores no fundo, credito "Developed by fabio_ad" e "PRESS START".
 *   Ao pressionar START/ENTER, vai para a tela de apresentacao.
 *
 * ALGORITMOS DE ANIMACAO:
 *   - Oscilacao vertical do titulo por seno (bob).
 *   - Pulsar de escala por seno.
 *   - Piscar do "PRESS START" por onda quadrada (mod do tempo).
 *
 * PORTABILIDADE:
 *   - SNES: efeitos assim sao feitos com HDMA (gradiente de fundo) e mudanca de
 *     posicao de OBJ por VBlank; o "ciclo de cores" e rotacao de paleta (CGRAM).
 */
public final class TitleScreen extends BaseGameScreen {

    private float time;

    public TitleScreen(DanielGame game) {
        super(game);
    }

    @Override
    public void show() {
        ctx.render.resetWorldCamera();
        ctx.audio.playMusic(AssetPaths.MUSIC_MENU, true);
    }

    @Override
    protected void update(float dt) {
        time += dt;
        GameInput in = ctx.input;
        if (in.isPressed(GameInput.Action.START)) {
            ctx.audio.playSfx("select");
            game.changeScreen(new PresentationScreen(game));
        }
    }

    @Override
    protected void draw() {
        ctx.render.applyHudProjection();

        float w = GameConfig.VIRTUAL_WIDTH;
        float h = GameConfig.VIRTUAL_HEIGHT;

        // Fundo com gradiente animado (duas faixas com cor variando no tempo).
        float hue = (float) (0.5f + 0.5f * Math.sin(time * 0.5f));
        ctx.render.fillRect(0, 0, w, h, new Color(0.05f, 0.05f, 0.15f + hue * 0.1f, 1f));
        ctx.render.fillRect(0, h * 0.55f, w, h * 0.45f, new Color(0.10f, 0.05f + hue * 0.1f, 0.30f, 1f));

        // Titulo com bob (posicao inteira) — escala 2x pixel.
        float bob = Math.round(Math.sin(time * 2f) * 4f);
        Color titleColor = new Color(1f, 0.85f - hue * 0.2f, 0.2f + hue * 0.3f, 1f);
        TextUtil.drawCentered(ctx.render.getBatch(), ctx.assets.getFont(),
                "DANIEL DO BOLO'S", w / 2f, h * 0.72f + bob, 0.5f, titleColor);
        TextUtil.drawCentered(ctx.render.getBatch(), ctx.assets.getFont(),
                "ADVENTURE", w / 2f, h * 0.58f + bob, 1.0f, titleColor);

        // Credito do autor.
        TextUtil.drawCentered(ctx.render.getBatch(), ctx.assets.getFont(),
                "Developed by " + GameConfig.AUTHOR, w / 2f, h * 0.32f, 0.5f, Color.WHITE);

        // "PRESS START" piscando.
        if (((int) (time * 2f)) % 2 == 0) {
            TextUtil.drawCentered(ctx.render.getBatch(), ctx.assets.getFont(),
                    "PRESS START", w / 2f, h * 0.18f, 0.5f, Color.WHITE);
        }
    }
}
