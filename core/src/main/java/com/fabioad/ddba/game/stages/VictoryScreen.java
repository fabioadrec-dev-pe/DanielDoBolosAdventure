package com.fabioad.ddba.game.stages;

import com.badlogic.gdx.graphics.Color;
import com.fabioad.ddba.engine.assets.AssetPaths;
import com.fabioad.ddba.engine.core.GameConfig;
import com.fabioad.ddba.engine.ui.TextUtil;
import com.fabioad.ddba.game.core.BaseGameScreen;
import com.fabioad.ddba.game.core.DanielGame;

/**
 * VictoryScreen (Creditos)
 * =============================================================================
 * OBJETIVO DA CLASSE:
 *   Segunda tela do epilogo: creditos rolando com pontuacao final, sobre o fundo
 *   velado da foto do iate. Fade-in → rolagem → fade-out → foto limpa.
 */
public final class VictoryScreen extends BaseGameScreen {

    private enum Phase { FADE_IN, SCROLL, FADE_OUT }

    private static final float FADE_SPEED = 0.9f;
    private static final float SCROLL_SPEED = 18f;
    private static final float LINE_SPACING = 13f;
    private static final float HOLD_AFTER = 1.5f;

    private static final String[] CREDITS = {
            "DANIEL DO BOLO'S ADVENTURE",
            "",
            "Voce venceu!",
            "",
            "GAME DESIGN",
            "fabio_ad",
            "",
            "PROGRAMACAO (JAVA / LibGDX)",
            "fabio_ad",
            "",
            "ARTE PIXEL (16 BITS)",
            "fabio_ad",
            "",
            "MUSICA & EFEITOS",
            "fabio_ad",
            "",
            "AMBIENTACAO",
            "Bairro Brasilia Teimosa - Recife/PE",
            "",
            "Obrigado por jogar!",
            "",
            "",
    };

    private Phase phase = Phase.FADE_IN;
    private float fade = 1f;
    private float scroll;
    private float hold;
    private float endScroll;

    public VictoryScreen(DanielGame game) {
        super(game);
    }

    @Override
    public void show() {
        ctx.render.resetWorldCamera();
        ctx.audio.playMusic(AssetPaths.MUSIC_VICTORY_FINAL, true);
        endScroll = (CREDITS.length - 1) * LINE_SPACING + GameConfig.VIRTUAL_HEIGHT + 8f;
        scroll = 0f;
        fade = 1f;
        hold = 0f;
        phase = Phase.FADE_IN;
    }

    @Override
    protected void update(float dt) {
        switch (phase) {
            case FADE_IN:
                fade = Math.max(0f, fade - FADE_SPEED * dt);
                if (fade <= 0f) phase = Phase.SCROLL;
                break;
            case SCROLL:
                scroll += SCROLL_SPEED * dt;
                if (scroll >= endScroll) {
                    scroll = endScroll;
                    hold += dt;
                    if (hold >= HOLD_AFTER) phase = Phase.FADE_OUT;
                }
                break;
            case FADE_OUT:
                fade = Math.min(1f, fade + FADE_SPEED * dt);
                if (fade >= 1f) {
                    game.changeScreen(new EndingPhotoScreen(game));
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected Color clearColor() {
        return Color.BLACK;
    }

    @Override
    protected void draw() {
        ctx.render.applyHudProjection();

        float w = GameConfig.VIRTUAL_WIDTH;
        float h = GameConfig.VIRTUAL_HEIGHT;

        drawEndingBackground(w, h, 0.55f);

        for (int i = 0; i < CREDITS.length; i++) {
            float y = scroll - i * LINE_SPACING;
            if (y < 22 || y > h - 10) continue;
            boolean header = CREDITS[i].equals(CREDITS[i].toUpperCase()) && !CREDITS[i].isEmpty();
            Color c = header ? Color.GOLD : Color.WHITE;
            TextUtil.drawCentered(ctx.render.getBatch(), ctx.assets.getFont(),
                    CREDITS[i], w / 2f, y, 0.5f, c);
        }

        TextUtil.drawCentered(ctx.render.getBatch(), ctx.assets.getFont(),
                "PONTUACAO FINAL: " + session.getScore(), w / 2f, 12, 0.5f, Color.CYAN);

        if (fade > 0.01f) {
            ctx.render.fillRect(0, 0, w, h, new Color(0f, 0f, 0f, fade));
        }
    }

    private void drawEndingBackground(float w, float h, float veilAlpha) {
        if (ctx.assets.endingBg != null) {
            ctx.render.getBatch().setColor(1f, 1f, 1f, 1f);
            ctx.render.getBatch().draw(ctx.assets.endingBg, 0, 0, w, h);
            ctx.render.fillRect(0, 0, w, h, new Color(0f, 0f, 0f, veilAlpha));
        }
    }
}
