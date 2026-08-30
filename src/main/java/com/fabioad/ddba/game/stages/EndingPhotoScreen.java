package com.fabioad.ddba.game.stages;

import com.badlogic.gdx.graphics.Color;
import com.fabioad.ddba.engine.core.GameConfig;
import com.fabioad.ddba.game.core.BaseGameScreen;
import com.fabioad.ddba.game.core.DanielGame;

/**
 * EndingPhotoScreen
 * =============================================================================
 * OBJETIVO DA CLASSE:
 *   Ultima tela do epilogo: foto do iate em tela cheia, sem véu. Fade-in →
 *   exibe → fade-out → menu.
 */
public final class EndingPhotoScreen extends BaseGameScreen {

    private enum Phase { FADE_IN, HOLD, FADE_OUT }

    private static final float FADE_SPEED = 0.75f;
    private static final float HOLD_TIME = 6f;

    private Phase phase = Phase.FADE_IN;
    private float fade = 1f;
    private float hold;

    public EndingPhotoScreen(DanielGame game) {
        super(game);
    }

    @Override
    public void show() {
        ctx.render.resetWorldCamera();
        fade = 1f;
        hold = 0f;
        phase = Phase.FADE_IN;
    }

    @Override
    protected void update(float dt) {
        switch (phase) {
            case FADE_IN:
                fade = Math.max(0f, fade - FADE_SPEED * dt);
                if (fade <= 0f) phase = Phase.HOLD;
                break;
            case HOLD:
                hold += dt;
                if (hold >= HOLD_TIME) phase = Phase.FADE_OUT;
                break;
            case FADE_OUT:
                fade = Math.min(1f, fade + FADE_SPEED * dt);
                if (fade >= 1f) {
                    game.changeScreen(new MenuScreen(game));
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

        if (ctx.assets.endingBg != null) {
            ctx.render.getBatch().setColor(1f, 1f, 1f, 1f);
            ctx.render.getBatch().draw(ctx.assets.endingBg, 0, 0, w, h);
        } else {
            ctx.render.fillRect(0, 0, w, h, Color.BLACK);
        }

        if (fade > 0.01f) {
            ctx.render.fillRect(0, 0, w, h, new Color(0f, 0f, 0f, fade));
        }
    }
}
