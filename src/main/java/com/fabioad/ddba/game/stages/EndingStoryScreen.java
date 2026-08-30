package com.fabioad.ddba.game.stages;

import com.badlogic.gdx.graphics.Color;
import com.fabioad.ddba.engine.assets.AssetPaths;
import com.fabioad.ddba.engine.core.GameConfig;
import com.fabioad.ddba.engine.ui.TextUtil;
import com.fabioad.ddba.game.core.BaseGameScreen;
import com.fabioad.ddba.game.core.DanielGame;

/**
 * EndingStoryScreen
 * =============================================================================
 * OBJETIVO DA CLASSE:
 *   Primeira tela do epilogo apos o stage 5: narrativa do "sonho realizado"
 *   sobre o fundo velado da foto do iate. Fade-in → rolagem → fade-out → creditos.
 *
 * ROLAGEM:
 *   Ordem de creditos classica: linha 0 (inicio do texto) entra por baixo e sobe;
 *   a ultima linha sobe por ultimo. Assim o texto inteiro passa legivel.
 */
public final class EndingStoryScreen extends BaseGameScreen {

    private enum Phase { FADE_IN, SCROLL, FADE_OUT }

    /**
     * Texto oficial (acentos convertidos para ASCII — a fonte pixel cobre Latin-1,
     * mas ASCII evita glifo ausente / medicao errada no wrap).
     */
    private static final String STORY =
            "Finalmente, depois de uma infancia que passou fome e aguentar "
                    + "clientes de sua barraca (quer dizer, APAPINHA), Daniel do Bolo, "
                    + "homem nascido e criado em Brasilia Teimosa, ganhou a Mega Sena, "
                    + "deu dinheiro para os parentes e amigos, e finalmente realiozou o "
                    + "sonho de comprar um iate e curtir festinhas em alto mar. "
                    + "Mas nao foi facil: precisou ir pra Noronha enfrentar como mestre "
                    + "final do jogo ARAPINHA (isso, o boneco feio que e o mestre so "
                    + "podia ser ele), que apos receber uma lapda de Serra Grannde "
                    + "resolveu ajudar com o sonho de Daniel.";

    private static final float FADE_SPEED = 0.9f;
    private static final float SCROLL_SPEED = 14f;
    private static final float LINE_SPACING = 12f;
    private static final float HOLD_AFTER = 2.0f;
    private static final float TOP_MARGIN = 28f;
    private static final float BOTTOM_MARGIN = 16f;

    private Phase phase = Phase.FADE_IN;
    private float fade = 1f;
    private float scroll;
    private float hold;
    private String[] lines;
    private float endScroll;

    public EndingStoryScreen(DanielGame game) {
        super(game);
    }

    @Override
    public void show() {
        ctx.render.resetWorldCamera();
        ctx.audio.playMusic(AssetPaths.MUSIC_VICTORY_FINAL, true);
        lines = TextUtil.wrap(ctx.assets.getFont(), STORY, 0.5f, GameConfig.VIRTUAL_WIDTH - 20f);
        // Linha 0 em y=scroll; termina quando a ultima linha passa do topo.
        endScroll = (lines.length - 1) * LINE_SPACING + GameConfig.VIRTUAL_HEIGHT - TOP_MARGIN + 8f;
        scroll = 0f;
        hold = 0f;
        fade = 1f;
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
                    game.changeScreen(new VictoryScreen(game));
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

        TextUtil.drawCentered(ctx.render.getBatch(), ctx.assets.getFont(),
                "O SONHO", w / 2f, h - 10, 0.5f, Color.GOLD);

        // Creditos: y = scroll - i*espaco (linha 0 sobe primeiro; ultima por ultimo).
        for (int i = 0; i < lines.length; i++) {
            float y = scroll - i * LINE_SPACING;
            if (y < BOTTOM_MARGIN || y > h - TOP_MARGIN) continue;
            TextUtil.drawCentered(ctx.render.getBatch(), ctx.assets.getFont(),
                    lines[i], w / 2f, y, 0.5f, Color.WHITE);
        }

        if (fade > 0.01f) {
            ctx.render.fillRect(0, 0, w, h, new Color(0f, 0f, 0f, fade));
        }
    }

    private void drawEndingBackground(float w, float h, float veilAlpha) {
        if (ctx.assets.endingBg != null) {
            ctx.render.getBatch().setColor(1f, 1f, 1f, 1f);
            ctx.render.getBatch().draw(ctx.assets.endingBg, 0, 0, w, h);
            ctx.render.fillRect(0, 0, w, h, new Color(0f, 0f, 0f, veilAlpha));
        } else {
            ctx.render.fillRect(0, 0, w, h, Color.BLACK);
        }
    }
}
