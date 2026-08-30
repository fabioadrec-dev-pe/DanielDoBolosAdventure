package com.fabioad.ddba.app;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration.GLEmulation;
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
 *   - No Windows, ANGLE (GLES via Direct3D) e o padrao: muitos PCs/VMs nao
 *     exposem OpenGL ("WGL: The driver does not appear to support OpenGL").
 *     Override: --gl  (forcar OpenGL)  |  --angle (forcar ANGLE).
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
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
            error.printStackTrace();
            writeErrorLog("Thread: " + thread.getName() + "\n" + stackTraceOf(error));
        });

        boolean useAngle = shouldUseAngle(args);

        try {
            startGame(useAngle);
        } catch (Throwable firstError) {
            // Se OpenGL nativo falhou, tenta ANGLE uma vez (ex.: Windows com driver quebrado).
            if (!useAngle && isLikelyMissingOpenGl(firstError)) {
                System.err.println("OpenGL indisponivel; tentando ANGLE (Direct3D)...");
                try {
                    startGame(true);
                    return;
                } catch (Throwable angleError) {
                    firstError.addSuppressed(angleError);
                }
            }
            firstError.printStackTrace();
            writeErrorLog(stackTraceOf(firstError));
            throw firstError;
        }
    }

    private static void startGame(boolean useAngle) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle(GameConfig.TITLE);

        int scale = GameConfig.DEFAULT_SCALE;
        config.setWindowedMode(GameConfig.VIRTUAL_WIDTH * scale, GameConfig.VIRTUAL_HEIGHT * scale);

        // Tamanho minimo = 1x (garante que o canvas 256x224 sempre caiba).
        config.setWindowSizeLimits(GameConfig.VIRTUAL_WIDTH, GameConfig.VIRTUAL_HEIGHT, -1, -1);

        config.useVsync(true);
        config.setForegroundFPS(60);
        config.setIdleFPS(30);
        config.setWindowIcon(
                "branding/icon_128.png",
                "branding/icon_64.png",
                "branding/icon_32.png",
                "branding/icon_16.png");

        if (useAngle) {
            config.setOpenGLEmulation(GLEmulation.ANGLE_GLES20, 0, 0);
        }

        new Lwjgl3Application(new DanielGame(), config);
    }

    /** Windows: ANGLE por padrao. --gl forca OpenGL; --angle forca ANGLE. */
    private static boolean shouldUseAngle(String[] args) {
        for (String arg : args) {
            if ("--gl".equalsIgnoreCase(arg)) {
                return false;
            }
            if ("--angle".equalsIgnoreCase(arg)) {
                return true;
            }
        }
        String prop = System.getProperty("ddba.gl", "").trim().toLowerCase();
        if ("gl".equals(prop) || "opengl".equals(prop)) {
            return false;
        }
        if ("angle".equals(prop)) {
            return true;
        }
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    private static boolean isLikelyMissingOpenGl(Throwable error) {
        for (Throwable t = error; t != null; t = t.getCause()) {
            String msg = String.valueOf(t.getMessage()).toLowerCase();
            if (msg.contains("couldn't create window")
                    || msg.contains("opengl")
                    || msg.contains("wgl")
                    || msg.contains("glfw_api_unavailable")) {
                return true;
            }
        }
        return false;
    }

    private static void writeErrorLog(String text) {
        try {
            java.nio.file.Files.writeString(java.nio.file.Path.of("erro.log"), text);
        } catch (Throwable ignored) {
            // best-effort log for portable Windows builds
        }
    }

    private static String stackTraceOf(Throwable error) {
        java.io.StringWriter writer = new java.io.StringWriter();
        error.printStackTrace(new java.io.PrintWriter(writer));
        return writer.toString();
    }

    private DesktopLauncher() {
    }
}
