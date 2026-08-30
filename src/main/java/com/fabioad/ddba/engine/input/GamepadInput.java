package com.fabioad.ddba.engine.input;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.controllers.Controllers;

/**
 * GamepadInput
 * =============================================================================
 * Leitura unificada de joysticks USB/BT via gdx-controllers (SDL/Jamepad no
 * desktop, Android InputDevice no mobile). Cobre Xbox, DualShock/DualSense,
 * Switch Pro / Joy-Con (emparelhados), PS3/PS4/PS5 via adaptadores e pads
 * genericos mapeados pelo SDL GameController DB.
 *
 * Mapeamento platformer (acoes do jogo):
 *   LEFT/RIGHT/UP/DOWN  — D-pad ou stick esquerdo
 *   JUMP                — A / X / R1  (botao sul + alternativos)
 *   RUN                 — B / Y / L1 / gatilho R
 *   START               — Start / Options / +
 *   SELECT / BACK       — Select / Share / - / View
 */
public final class GamepadInput {

    private static final float STICK_DEADZONE = 0.38f;
    private static final float TRIGGER_DEADZONE = 0.35f;

    private GamepadInput() {
    }

    /** True se houver pelo menos um controle fisico conectado. */
    public static boolean isConnected() {
        try {
            return Controllers.getControllers().size > 0;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean isDown(GameInput.Action action) {
        try {
            for (Controller c : Controllers.getControllers()) {
                if (c != null && isDown(c, action)) {
                    return true;
                }
            }
        } catch (Throwable ignored) {
            // Driver/JNI pode falhar em hotplug; ignora neste frame.
        }
        return false;
    }

    private static boolean isDown(Controller c, GameInput.Action action) {
        ControllerMapping m = c.getMapping();
        switch (action) {
            case LEFT:
                return dpadOrStick(c, m, -1, 0);
            case RIGHT:
                return dpadOrStick(c, m, 1, 0);
            case UP:
                return dpadOrStick(c, m, 0, 1);
            case DOWN:
                return dpadOrStick(c, m, 0, -1);
            case JUMP:
                return button(c, m != null ? m.buttonA : -1)
                        || button(c, m != null ? m.buttonX : -1)
                        || button(c, m != null ? m.buttonR1 : -1)
                        || fallbackFace(c, true);
            case RUN:
                // gdx-controllers 2.2.2: L2/R2 sao botoes no ControllerMapping (sem axisLeft/RightTrigger).
                return button(c, m != null ? m.buttonB : -1)
                        || button(c, m != null ? m.buttonY : -1)
                        || button(c, m != null ? m.buttonL1 : -1)
                        || button(c, m != null ? m.buttonR2 : -1)
                        || button(c, m != null ? m.buttonL2 : -1)
                        || trigger(c, 4) // fallback: alguns pads exposem gatilho como eixo
                        || trigger(c, 5)
                        || fallbackFace(c, false);
            case START:
                // Sem buttonGuide na API 2.2.2; usa Start + indices comuns (Options/+/Guide).
                return button(c, m != null ? m.buttonStart : -1)
                        || fallbackIndex(c, 6, 7, 9, 11);
            case SELECT:
                return button(c, m != null ? m.buttonBack : -1)
                        || fallbackIndex(c, 4, 8, 10);
            case BACK:
                return button(c, m != null ? m.buttonBack : -1)
                        || fallbackIndex(c, 4, 8, 10);
            default:
                return false;
        }
    }

    /** dirX/dirY: -1, 0 ou +1 no eixo desejado (Y+ = cima). */
    private static boolean dpadOrStick(Controller c, ControllerMapping m, int dirX, int dirY) {
        if (m != null) {
            if (dirX < 0 && button(c, m.buttonDpadLeft)) return true;
            if (dirX > 0 && button(c, m.buttonDpadRight)) return true;
            if (dirY > 0 && button(c, m.buttonDpadUp)) return true;
            if (dirY < 0 && button(c, m.buttonDpadDown)) return true;

            if (dirX != 0 && m.axisLeftX >= 0) {
                float ax = c.getAxis(m.axisLeftX);
                if (dirX < 0 && ax < -STICK_DEADZONE) return true;
                if (dirX > 0 && ax > STICK_DEADZONE) return true;
            }
            if (dirY != 0 && m.axisLeftY >= 0) {
                // SDL/Jamepad: Y+ costuma ser para baixo; invertemos para cima=positivo lógico.
                float ay = -c.getAxis(m.axisLeftY);
                if (dirY > 0 && ay > STICK_DEADZONE) return true;
                if (dirY < 0 && ay < -STICK_DEADZONE) return true;
            }
        }

        // Fallback generico: eixos 0/1 + botoes D-pad comuns.
        if (dirX != 0) {
            float ax = safeAxis(c, 0);
            if (dirX < 0 && ax < -STICK_DEADZONE) return true;
            if (dirX > 0 && ax > STICK_DEADZONE) return true;
            if (dirX < 0 && fallbackIndex(c, 13, 15)) return true;
            if (dirX > 0 && fallbackIndex(c, 14, 16)) return true;
        }
        if (dirY != 0) {
            float ay = -safeAxis(c, 1);
            if (dirY > 0 && ay > STICK_DEADZONE) return true;
            if (dirY < 0 && ay < -STICK_DEADZONE) return true;
            if (dirY > 0 && fallbackIndex(c, 11, 12)) return true;
            if (dirY < 0 && fallbackIndex(c, 12, 13)) return true;
        }
        return false;
    }

    private static boolean button(Controller c, int code) {
        if (code < 0) return false;
        try {
            return c.getButton(code);
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean trigger(Controller c, int axis) {
        if (axis < 0) return false;
        try {
            return c.getAxis(axis) > TRIGGER_DEADZONE;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static float safeAxis(Controller c, int axis) {
        try {
            if (axis < 0 || axis >= c.getAxisCount()) return 0f;
            return c.getAxis(axis);
        } catch (Throwable ignored) {
            return 0f;
        }
    }

    /** Face buttons sem mapping: indices tipicos SDL (0=A/sul, 1=B/leste). */
    private static boolean fallbackFace(Controller c, boolean jump) {
        if (jump) {
            return fallbackIndex(c, 0, 2, 3);
        }
        return fallbackIndex(c, 1, 3, 2);
    }

    private static boolean fallbackIndex(Controller c, int... indices) {
        for (int i : indices) {
            try {
                if (i >= 0 && i <= c.getMaxButtonIndex() && c.getButton(i)) {
                    return true;
                }
            } catch (Throwable ignored) {
                // ignore
            }
        }
        return false;
    }
}
