package com.fabioad.ddba.engine.core;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.fabioad.ddba.engine.renderer.LetterboxInfo;

/**
 * AbstractScreen
 * =============================================================================
 * OBJETIVO DA CLASSE:
 *   Base para TODAS as telas (menu, apresentacao, fase, game over...). Implementa
 *   o LOOP PRINCIPAL com PASSO FIXO (fixed timestep) e o pipeline de renderizacao
 *   pixel-perfect, deixando as subclasses apenas com update(dt) e draw().
 *
 * ALGORITMO (fixed timestep com acumulador - Gaffer "Fix Your Timestep!"):
 *   A cada quadro somamos o tempo real (delta) a um acumulador. Enquanto houver
 *   >= FIXED_TIMESTEP acumulado, executamos um passo LOGICO de tamanho fixo.
 *   Isso torna a fisica DETERMINISTICA e estavel, independente do FPS de video.
 *   A renderizacao ocorre uma vez por quadro (com o estado mais recente).
 *
 * DECISOES DE ARQUITETURA:
 *   - Template Method: render() (final) orquestra; subclasses preenchem os "buracos"
 *     update()/draw()/clearColor().
 *   - A entrada e atualizada uma vez por quadro (snapshot coerente).
 *
 * PORTABILIDADE:
 *   - SNES: o "passo fixo" e natural: o jogo roda 1 quadro logico por VBlank (60Hz
 *     NTSC / 50Hz PAL). Aqui replicamos esse determinismo em hardware variavel.
 */
public abstract class AbstractScreen implements Screen {

    protected final GameContext ctx;

    /** Acumulador de tempo para o passo fixo. */
    private float accumulator;

    protected AbstractScreen(GameContext ctx) {
        this.ctx = ctx;
    }

    /** Cor de fundo do canvas virtual (subclasses podem sobrescrever). */
    protected Color clearColor() {
        return Color.BLACK;
    }

    /** Logica do jogo em passo fixo. dt == GameConfig.FIXED_TIMESTEP sempre. */
    protected abstract void update(float dt);

    /**
     * Desenho da cena (mundo + HUD) dentro do canvas virtual 256x224.
     * O SpriteBatch ja esta ativo (entre begin/end) quando este metodo roda.
     */
    protected abstract void draw();

    @Override
    public final void render(float delta) {
        // 1) Snapshot de entrada (coerente para todo o quadro).
        //    Passa o RenderContext para o joypad virtual mapear toques no canvas.
        ctx.input.update(ctx.render);

        // 2) Passos logicos de tamanho fixo (evita "spiral of death" com clamp).
        accumulator += Math.min(delta, GameConfig.MAX_FRAME_TIME);
        while (accumulator >= GameConfig.FIXED_TIMESTEP) {
            update(GameConfig.FIXED_TIMESTEP);
            accumulator -= GameConfig.FIXED_TIMESTEP;
        }

        // 3) Mundo no FBO → presenta → overlay do joypad nas barras pretas.
        ctx.render.beginWorld(clearColor());
        draw();
        ctx.render.endWorld();
        LetterboxInfo box = ctx.render.present();
        ctx.input.drawTouchOverlayScreen(ctx.render, ctx.assets.getFont(), box);
    }

    // ---- Ciclo de vida do Screen (defaults; subclasses sobrescrevem se preciso) ----
    @Override public void show() { }
    @Override public void resize(int width, int height) { }
    @Override public void pause() { }
    @Override public void resume() { }
    @Override public void hide() { }
    @Override public void dispose() { }
}
