package com.fabioad.ddba.app;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.fabioad.ddba.engine.core.GameConfig;
import com.fabioad.ddba.game.core.DanielGame;

/**
 * DesktopLauncher (modulos "app" + "launcher")
 * =============================================================================
 * OBJETIVO DA CLASSE:
 *   Ponto de entrada (main) da versao DESKTOP. Cria a janela via backend LWJGL3
 *   (OpenGL + OpenAL + GLFW) e inicia o jogo (DanielGame). E a UNICA classe
 *   dependente de plataforma; toda a logica vive em engine/game (portaveis).
 *
 * DECISOES DE ARQUITETURA:
 *   - Separar o "launcher" do "jogo" permite ter outros launchers (Android, HTML)
 *     reutilizando exatamente o mesmo DanielGame. Fiel a arquitetura do LibGDX.
 *   - Janela inicia em escala inteira (256*3 x 224*3) com VSync ligado.
 *
 * CONFIGURACOES DE RENDER:
 *   - VSync: sincroniza com o monitor (evita tearing e "gasta" menos CPU/GPU).
 *   - foregroundFPS 60: casa com o passo fixo logico (60 Hz), estilo NTSC.
 *
 * PORTABILIDADE:
 *   - Android: substituir por AndroidLauncher (AndroidApplication).
 *   - HTML: GwtApplication. Switch/consoles: backend nativo equivalente.
 *   - SNES/68000: nao ha "launcher"; a ROM inicia no vetor de RESET do CPU.
 */
public final class DesktopLauncher {

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle(GameConfig.TITLE);

        int scale = GameConfig.DEFAULT_SCALE;
        config.setWindowedMode(GameConfig.VIRTUAL_WIDTH * scale, GameConfig.VIRTUAL_HEIGHT * scale);

        // Tamanho minimo = 1x (garante que o canvas 256x224 sempre caiba).
        config.setWindowSizeLimits(GameConfig.VIRTUAL_WIDTH, GameConfig.VIRTUAL_HEIGHT, -1, -1);

        config.useVsync(true);
        config.setForegroundFPS(60);
        config.setIdleFPS(30);

        new Lwjgl3Application(new DanielGame(), config);
    }

    private DesktopLauncher() {
    }
}
