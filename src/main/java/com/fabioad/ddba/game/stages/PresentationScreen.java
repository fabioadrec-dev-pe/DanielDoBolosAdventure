package com.fabioad.ddba.game.stages;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.fabioad.ddba.engine.core.GameConfig;
import com.fabioad.ddba.engine.input.GameInput;
import com.fabioad.ddba.engine.ui.TextUtil;
import com.fabioad.ddba.game.core.BaseGameScreen;
import com.fabioad.ddba.game.core.DanielGame;
import com.fabioad.ddba.game.data.BrasiliaTeimosaStreets;

/**
 * PresentationScreen (Tela de Apresentacao)
 * =============================================================================
 * OBJETIVO DA CLASSE:
 *   Apresentacao animada em fundo preto, homenageando o bairro Brasilia Teimosa
 *   ao rolar (scroll) a lista de ruas, com efeitos classicos de demo 16 bits:
 *     - FADE IN / FADE OUT (overlay preto com alpha)
 *     - SCROLL (rolagem vertical da lista)
 *     - ZOOM (escala pulsante de um sprite)
 *     - ROTACAO (giro de um sprite)
 *     - MUDANCA DE PALETA (ciclo de cores)
 *     - MOSAICO (simulado por grade de blocos que "resolve" no inicio)
 *   START/ENTER retorna ao menu principal.
 *
 * PORTABILIDADE (como cada efeito seria no hardware real):
 *   - FADE: escrever a paleta escurecendo (SNES: CGRAM; MD: CRAM) por VBlank.
 *   - SCROLL: registradores BGxVOFS (SNES) / VSCROLL (MD).
 *   - ZOOM/ROTACAO: Modo 7 (SNES) ou celulas de sprite escaladas.
 *   - PALETA: rotacao de cores na CGRAM/CRAM.
 *   - MOSAICO: registrador MOSAIC do SNES (aqui simulamos por software).
 */
public final class PresentationScreen extends BaseGameScreen {

    private float time;
    private float scroll;
    private boolean leaving;
    private float fade = 1f; // 1 = preto total (comeca em fade-in)

    private static final float SCROLL_SPEED = 18f;
    private static final float LINE_SPACING = 16f;

    public PresentationScreen(DanielGame game) {
        super(game);
    }

    @Override
    public void show() {
        ctx.render.resetWorldCamera();
    }

    @Override
    protected void update(float dt) {
        time += dt;
        scroll += SCROLL_SPEED * dt;

        // Fade-in nos primeiros ~1s; fade-out ao sair.
        if (leaving) {
            fade = Math.min(1f, fade + dt * 1.5f);
            if (fade >= 1f) {
                game.changeScreen(new MenuScreen(game));
            }
        } else {
            fade = Math.max(0f, fade - dt * 1.0f);
        }

        if (!leaving && ctx.input.isPressed(GameInput.Action.START)) {
            ctx.audio.playSfx("select");
            leaving = true;
        }
    }

    @Override
    protected Color clearColor() {
        return Color.BLACK; // fundo preto exigido pela apresentacao
    }

    @Override
    protected void draw() {
        ctx.render.applyHudProjection();

        float w = GameConfig.VIRTUAL_WIDTH;
        float h = GameConfig.VIRTUAL_HEIGHT;

        drawStreetBackground(w, h);

        // --- MUDANCA DE PALETA: cor-base cicla com o tempo ---
        Color palette = paletteCycle(time);

        // --- Titulo do bloco ---
        TextUtil.drawCentered(ctx.render.getBatch(), ctx.assets.getFont(),
                "BRASILIA TEIMOSA", w / 2f, h - 16, 0.5f, palette);
        TextUtil.drawCentered(ctx.render.getBatch(), ctx.assets.getFont(),
                "As ruas do bairro", w / 2f, h - 30, 0.5f, Color.LIGHT_GRAY);

        // --- SCROLL da lista de ruas (rola de baixo para cima, em loop) ---
        String[] streets = BrasiliaTeimosaStreets.STREETS;
        float total = streets.length * LINE_SPACING;
        for (int i = 0; i < streets.length; i++) {
            float baseY = 40 + i * LINE_SPACING;
            // Posicao com scroll circular (reaparece por cima).
            float y = ((baseY - scroll) % (total + h));
            if (y < 0) y += (total + h);
            // Fade nas bordas (aparece/some suavemente).
            float edgeAlpha = edgeFade(y, h);
            Color c = new Color(palette.r, palette.g, palette.b, edgeAlpha);
            TextUtil.drawCentered(ctx.render.getBatch(), ctx.assets.getFont(),
                    streets[i], w / 2f, y, 0.5f, c);
        }

        // --- ZOOM + ROTACAO + PALETA: uma moeda girando e pulsando ---
        drawSpinningEmblem(w / 2f, h * 0.5f, palette);

        // --- MOSAICO simulado (so no inicio, "resolve" nos primeiros 1.5s) ---
        drawMosaic(w, h);

        // --- FADE IN/OUT (overlay preto por cima de tudo) ---
        if (fade > 0f) {
            ctx.render.fillRect(0, 0, w, h, new Color(0, 0, 0, fade));
        }

        // Rodape.
        if (!leaving && ((int) (time * 2f)) % 2 == 0) {
            TextUtil.drawCentered(ctx.render.getBatch(), ctx.assets.getFont(),
                    "PRESS START", w / 2f, 8, 0.5f, Color.WHITE);
        }
    }

    /** Ciclo de cores (simula rotacao de paleta). */
    private Color paletteCycle(float t) {
        float r = 0.5f + 0.5f * (float) Math.sin(t * 1.1f);
        float g = 0.5f + 0.5f * (float) Math.sin(t * 1.1f + 2.09f);
        float b = 0.5f + 0.5f * (float) Math.sin(t * 1.1f + 4.18f);
        return new Color(0.4f + r * 0.6f, 0.4f + g * 0.6f, 0.4f + b * 0.6f, 1f);
    }

    /** Alpha que suaviza aparicao/sumico nas bordas superior/inferior. */
    private float edgeFade(float y, float h) {
        float margin = 40f;
        float a = 1f;
        if (y < margin) a = y / margin;
        else if (y > h - margin) a = (h - y) / margin;
        return Math.max(0f, Math.min(1f, a));
    }

    /** Desenha uma moeda com zoom (escala) e rotacao, cor da paleta atual. */
    private void drawSpinningEmblem(float cx, float cy, Color palette) {
        Animation<TextureRegion> coin = ctx.assets.coin;
        if (coin == null) return;
        TextureRegion frame = coin.getKeyFrame(time, true);
        float scale = 2.5f + (float) Math.sin(time * 2f) * 1.2f; // ZOOM
        float rotation = time * 120f;                            // ROTACAO
        float size = 16f;
        ctx.render.getBatch().setColor(palette);
        ctx.render.getBatch().draw(frame,
                cx - size / 2f, cy - size / 2f, // posicao
                size / 2f, size / 2f,           // origem (centro) p/ rotacionar
                size, size,                     // largura/altura
                scale, scale,                   // escala (zoom)
                rotation);                      // rotacao em graus
        ctx.render.getBatch().setColor(Color.WHITE);
    }

    /**
     * MOSAICO simulado: nos primeiros 1.5s, cobre a tela com uma grade de blocos
     * pretos que vao encolhendo (revelando a arte), imitando o efeito MOSAIC.
     */
    private void drawMosaic(float w, float h) {
        float phase = 1.5f - time;
        if (phase <= 0f) return;
        float t = Math.min(1f, phase / 1.5f); // 1 -> 0
        int block = Math.max(2, (int) (2 + t * 30)); // blocos grandes -> pequenos
        for (int y = 0; y < h; y += block) {
            for (int x = 0; x < w; x += block) {
                // Padrao xadrez para dar textura ao mosaico.
                if (((x / block) + (y / block)) % 2 == 0) {
                    ctx.render.fillRect(x, y, block, block, new Color(0, 0, 0, t));
                }
            }
        }
    }

    /** Mesmo fundo pixelizado do menu (Daniel), com véu escuro para legibilidade. */
    private void drawStreetBackground(float w, float h) {
        if (ctx.assets.menuBg != null) {
            ctx.render.getBatch().setColor(Color.WHITE);
            ctx.render.getBatch().draw(ctx.assets.menuBg, 0, 0, w, h);
            ctx.render.fillRect(0, 0, w, h, new Color(0f, 0f, 0f, 0.50f));
        } else {
            ctx.render.fillRect(0, 0, w, h, Color.BLACK);
        }
    }
}
