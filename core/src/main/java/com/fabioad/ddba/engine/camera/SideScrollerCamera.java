package com.fabioad.ddba.engine.camera;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.fabioad.ddba.engine.core.GameConfig;

/**
 * SideScrollerCamera
 * =============================================================================
 * OBJETIVO DA CLASSE:
 *   Controlar a camera lateral (scroll horizontal/vertical) que segue o jogador,
 *   com suavizacao (lerp) e "travamento" (clamp) nos limites da fase, para nunca
 *   mostrar o "vazio" fora do mapa.
 *
 * ALGORITMO:
 *   1) Alvo = posicao do jogador (centro).
 *   2) A camera se move em direcao ao alvo por interpolacao linear (lerp) com
 *      fator dependente de dt (suavidade constante independente de FPS).
 *   3) O centro da camera e limitado (clamp) para que a borda da viewport nunca
 *      ultrapasse os limites do mundo [0..levelWidth] x [0..levelHeight].
 *
 * DECISOES DE ARQUITETURA:
 *   - A camera opera sobre a OrthographicCamera do RenderContext (nao cria outra),
 *     mantendo uma unica fonte de verdade para a projecao.
 *   - Suavizacao opcional; para o "feel" classico 16 bits, um lerp leve funciona.
 *
 * PORTABILIDADE:
 *   - SNES: equivale a escrever nos registradores de scroll BG1HOFS/BG1VOFS a
 *     cada VBlank. O "clamp" evita mostrar tiles fora do tilemap.
 *   - Mega Drive: registradores de scroll do VDP (tabelas HSCROLL/VSCROLL).
 */
public final class SideScrollerCamera {

    private final OrthographicCamera camera;

    /** Limites do mundo em pixels (definidos ao carregar a fase). */
    private float worldWidth;
    private float worldHeight;

    /** Fator de suavizacao (0 = travado no alvo; maior = mais "solto"). */
    private float smoothing = 8f;

    public SideScrollerCamera(OrthographicCamera camera) {
        this.camera = camera;
    }

    /** Define os limites da fase (chamado ao carregar o mapa). */
    public void setWorldBounds(float worldWidth, float worldHeight) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
    }

    public void setSmoothing(float smoothing) {
        this.smoothing = smoothing;
    }

    /** Posiciona a camera imediatamente no alvo (sem suavizar), com clamp. */
    public void snapTo(float targetX, float targetY) {
        camera.position.set(clampX(targetX), clampY(targetY), 0);
        camera.update();
    }

    /**
     * Atualiza a camera seguindo o alvo suavemente.
     *
     * @param targetX centro X desejado (ex.: centro do jogador)
     * @param targetY centro Y desejado
     * @param dt      tempo do quadro
     */
    public void follow(float targetX, float targetY, float dt) {
        float tx = clampX(targetX);
        float ty = clampY(targetY);

        // Lerp estavel em relacao ao FPS: alpha = 1 - e^(-k*dt).
        float alpha = 1f - (float) Math.exp(-smoothing * dt);
        camera.position.x += (tx - camera.position.x) * alpha;
        camera.position.y += (ty - camera.position.y) * alpha;

        // Re-clamp apos o lerp (o alvo ja estava clampeado, mas garante bordas).
        camera.position.x = clampX(camera.position.x);
        camera.position.y = clampY(camera.position.y);
        camera.update();
    }

    private float clampX(float x) {
        float half = GameConfig.VIRTUAL_WIDTH / 2f;
        if (worldWidth <= GameConfig.VIRTUAL_WIDTH) {
            return half; // mundo menor que a tela: centraliza
        }
        return MathUtils.clamp(x, half, worldWidth - half);
    }

    private float clampY(float y) {
        float half = GameConfig.VIRTUAL_HEIGHT / 2f;
        if (worldHeight <= GameConfig.VIRTUAL_HEIGHT) {
            return half;
        }
        return MathUtils.clamp(y, half, worldHeight - half);
    }
}
