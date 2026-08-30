package com.fabioad.ddba.engine.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.fabioad.ddba.engine.core.GameConfig;

/**
 * RenderContext
 * =============================================================================
 * OBJETIVO DA CLASSE:
 *   Implementar a "camera virtual" e o pipeline de renderizacao PIXEL-PERFECT
 *   com ESCALONAMENTO INTEIRO (integer scaling), reproduzindo a estetica SNES.
 *
 * COMO FUNCIONA (algoritmo):
 *   1) Todo o jogo e desenhado num FrameBuffer (FBO) de exatamente 256x224.
 *      -> este e o "canvas virtual"; nele 1 unidade = 1 pixel de tela SNES.
 *   2) No fim do quadro, esse FBO e desenhado na janela real ampliado por um
 *      fator INTEIRO (1x, 2x, 3x...), o maior que caiba na janela.
 *   3) O restante da janela vira barras pretas (letterbox/pillarbox), mantendo
 *      o aspecto original e evitando distorcao / pixels "quebrados".
 *   Usa-se filtro NEAREST (sem suavizacao) para pixels nitidos.
 *
 * DECISOES DE ARQUITETURA:
 *   - Separar "camera do mundo" (worldCamera) da "camera de tela" (screenCamera).
 *   - Expor um unico SpriteBatch reutilizavel (evita alocacoes por quadro).
 *   - Fornecer uma textura branca 1x1 (whitePixel) para desenhar retangulos
 *     solidos (fundos, fades, barras de HUD) sem shapes extras.
 *
 * PORTABILIDADE:
 *   - SNES: nao existe "FBO"; o PPU ja escreve direto nos 256x224. O conceito de
 *     "camera do mundo" corresponde aos registradores de scroll BGxHOFS/BGxVOFS.
 *   - PC/Mobile/Switch: este mesmo padrao (render-to-texture + upscale) e o modo
 *     canonico de emular consoles retro em hardware moderno.
 */
public final class RenderContext implements Disposable {

    /** Lote de desenho 2D (batching de sprites para performance). */
    private final SpriteBatch batch;

    /** Camera do mundo virtual (256x224), origem no canto inferior-esquerdo. */
    private final OrthographicCamera worldCamera;

    /** Camera usada para desenhar o FBO final na janela real. */
    private final OrthographicCamera screenCamera;

    /**
     * Camera ESTATICA da HUD (0..256 x 0..224). Nunca se move com o scroll.
     * A HUD (vidas, moedas, tempo) precisa ficar fixa na tela, independente da
     * posicao da camera do mundo. No SNES isso corresponde a um BG separado com
     * scroll travado, ou a sprites de OBJ com prioridade alta.
     */
    private final OrthographicCamera hudCamera;

    /** Buffer off-screen onde o jogo e desenhado em resolucao nativa. */
    private FrameBuffer fbo;

    /** Textura branca 1x1 para retangulos solidos (fades, HUD, fundos). */
    private final Texture whitePixel;

    public RenderContext() {
        this.batch = new SpriteBatch();

        this.worldCamera = new OrthographicCamera();
        // setToOrtho(false, ...) => Y cresce para CIMA (matematico), origem embaixo.
        this.worldCamera.setToOrtho(false, GameConfig.VIRTUAL_WIDTH, GameConfig.VIRTUAL_HEIGHT);

        this.screenCamera = new OrthographicCamera();

        this.hudCamera = new OrthographicCamera();
        this.hudCamera.setToOrtho(false, GameConfig.VIRTUAL_WIDTH, GameConfig.VIRTUAL_HEIGHT);
        this.hudCamera.update();

        this.fbo = new FrameBuffer(Pixmap.Format.RGBA8888,
                GameConfig.VIRTUAL_WIDTH, GameConfig.VIRTUAL_HEIGHT, false);
        // Filtro NEAREST: pixels nitidos ao ampliar (essencial para pixel art).
        this.fbo.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        this.whitePixel = createWhitePixel();
    }

    /** Cria uma textura branca 1x1 (usada para desenhar retangulos coloridos). */
    private static Texture createWhitePixel() {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    /**
     * Inicia o desenho do MUNDO no FBO virtual.
     * Chamar no comeco do render de cada tela, antes de desenhar sprites.
     *
     * @param clear cor de fundo do canvas virtual.
     */
    public void beginWorld(Color clear) {
        fbo.begin();
        Gdx.gl.glClearColor(clear.r, clear.g, clear.b, clear.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        worldCamera.update();
        batch.setProjectionMatrix(worldCamera.combined);
        batch.begin();
    }

    /** Encerra o desenho do mundo (fecha batch e FBO). */
    public void endWorld() {
        batch.end();
        fbo.end();
    }

    /**
     * Apresenta o FBO na janela real com escala inteira e letterbox.
     * ALGORITMO:
     *   scale = maior inteiro tal que 256*scale <= larguraJanela
     *                              e   224*scale <= alturaJanela.
     *   Centraliza o resultado; o entorno fica preto.
     */
    public void present() {
        int winW = Gdx.graphics.getWidth();
        int winH = Gdx.graphics.getHeight();

        int scaleX = Math.max(1, winW / GameConfig.VIRTUAL_WIDTH);
        int scaleY = Math.max(1, winH / GameConfig.VIRTUAL_HEIGHT);
        int scale = Math.min(scaleX, scaleY); // escala inteira uniforme

        int drawW = GameConfig.VIRTUAL_WIDTH * scale;
        int drawH = GameConfig.VIRTUAL_HEIGHT * scale;
        int offsetX = (winW - drawW) / 2;
        int offsetY = (winH - drawH) / 2;

        // Limpa a janela inteira de preto (barras de letterbox/pillarbox).
        Gdx.gl.glViewport(0, 0, winW, winH);
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        screenCamera.setToOrtho(false, winW, winH);
        screenCamera.update();
        batch.setProjectionMatrix(screenCamera.combined);

        // A textura do FBO vem "de cabeca para baixo"; TextureRegion com flipY.
        TextureRegion region = new TextureRegion(fbo.getColorBufferTexture());
        region.flip(false, true);

        batch.begin();
        batch.setColor(Color.WHITE);
        batch.draw(region, offsetX, offsetY, drawW, drawH);
        batch.end();
    }

    /**
     * Converte coordenadas de tela (mouse/toque) para o mundo virtual 256x224.
     * Util para menus com clique ou ferramentas de debug.
     */
    public Vector3 screenToVirtual(int screenX, int screenY) {
        int winW = Gdx.graphics.getWidth();
        int winH = Gdx.graphics.getHeight();
        int scale = Math.min(Math.max(1, winW / GameConfig.VIRTUAL_WIDTH),
                Math.max(1, winH / GameConfig.VIRTUAL_HEIGHT));
        int drawW = GameConfig.VIRTUAL_WIDTH * scale;
        int drawH = GameConfig.VIRTUAL_HEIGHT * scale;
        int offsetX = (winW - drawW) / 2;
        int offsetY = (winH - drawH) / 2;
        float vx = (screenX - offsetX) / (float) scale;
        // Y da tela cresce para baixo; convertendo para Y-up virtual.
        float vy = GameConfig.VIRTUAL_HEIGHT - (screenY - offsetY) / (float) scale;
        return new Vector3(vx, vy, 0);
    }

    /**
     * Desenha um retangulo solido colorido usando a textura branca 1x1.
     * Util para fundos, barras da HUD e overlays de fade. Requer batch ativo.
     * Restaura a cor do batch para branco ao final.
     */
    public void fillRect(float x, float y, float w, float h, Color color) {
        batch.setColor(color);
        batch.draw(whitePixel, x, y, w, h);
        batch.setColor(Color.WHITE);
    }

    /** Aplica a projecao da camera do MUNDO (afetada pelo scroll da fase). */
    public void applyWorldProjection() {
        worldCamera.update();
        batch.setProjectionMatrix(worldCamera.combined);
    }

    /** Aplica a projecao ESTATICA da HUD (coordenadas fixas 0..256 x 0..224). */
    public void applyHudProjection() {
        hudCamera.update();
        batch.setProjectionMatrix(hudCamera.combined);
    }

    /**
     * Reposiciona a camera do mundo no centro do canvas virtual (como no inicio).
     * Necessario ao voltar de uma fase: o scroll da SideScrollerCamera deixa a
     * worldCamera longe; menus/UI desenhando em (0..256) sem reset ficariam PRETO.
     */
    public void resetWorldCamera() {
        worldCamera.position.set(GameConfig.VIRTUAL_WIDTH / 2f, GameConfig.VIRTUAL_HEIGHT / 2f, 0f);
        worldCamera.zoom = 1f;
        worldCamera.update();
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public OrthographicCamera getWorldCamera() {
        return worldCamera;
    }

    public Texture getWhitePixel() {
        return whitePixel;
    }

    @Override
    public void dispose() {
        batch.dispose();
        fbo.dispose();
        whitePixel.dispose();
    }
}
