package com.fabioad.ddba.engine.input;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.fabioad.ddba.engine.renderer.LetterboxInfo;
import com.fabioad.ddba.engine.renderer.RenderContext;

import java.util.function.BooleanSupplier;

/**
 * GameInput
 * =============================================================================
 * Teclado + gamepad fisico (USB/BT: Xbox, Switch, PS, generico) + joypad virtual
 * Android. O overlay some se teclado/controle fisico estiver conectado.
 */
public final class GameInput {

    public enum Action {
        LEFT, RIGHT, UP, DOWN,
        JUMP,
        RUN,
        START,
        SELECT,
        BACK
    }

    private final boolean[] current = new boolean[Action.values().length];
    private final boolean[] previous = new boolean[Action.values().length];
    private final VirtualGamepad touchPad = new VirtualGamepad();

    private RenderContext renderRef;
    private LetterboxInfo lastBox;

    /** Probe opcional (Android nativo) alem do gdx-controllers. */
    private static BooleanSupplier gamepadProbe = () -> false;

    public static void setGamepadProbe(BooleanSupplier probe) {
        gamepadProbe = probe != null ? probe : () -> false;
    }

    public GameInput() {
        boolean android = Gdx.app != null
                && Gdx.app.getType() == Application.ApplicationType.Android;
        touchPad.setPlatformEnabled(android);
    }

    public void update(RenderContext render) {
        this.renderRef = render;
        boolean android = Gdx.app != null
                && Gdx.app.getType() == Application.ApplicationType.Android;
        touchPad.setPlatformEnabled(android);

        boolean physicalConnected = isPhysicalControlsConnected();
        touchPad.setVisible(!physicalConnected);

        if (render != null) {
            lastBox = render.currentLetterbox();
            touchPad.update(lastBox);
        }

        System.arraycopy(current, 0, previous, 0, current.length);
        for (Action a : Action.values()) {
            boolean key = readKeyboard(a);
            boolean pad = GamepadInput.isDown(a);
            boolean touch = touchPad.isDown(a);
            current[a.ordinal()] = key || pad || touch;
        }
    }

    public void update() {
        update(renderRef);
    }

    private static boolean isPhysicalControlsConnected() {
        if (Gdx.input.isPeripheralAvailable(Input.Peripheral.HardwareKeyboard)) {
            return true;
        }
        if (GamepadInput.isConnected()) {
            return true;
        }
        try {
            if (gamepadProbe.getAsBoolean()) return true;
        } catch (Exception ignored) {
            // probe pode falhar em introspecao
        }
        return false;
    }

    private boolean readKeyboard(Action a) {
        // Teclado + keycodes Android de gamepad (alguns pads chegam como KEYCODE_BUTTON_*).
        switch (a) {
            case LEFT:
                return Gdx.input.isKeyPressed(Input.Keys.LEFT)
                        || Gdx.input.isKeyPressed(Input.Keys.A)
                        || Gdx.input.isKeyPressed(Input.Keys.DPAD_LEFT);
            case RIGHT:
                return Gdx.input.isKeyPressed(Input.Keys.RIGHT)
                        || Gdx.input.isKeyPressed(Input.Keys.D)
                        || Gdx.input.isKeyPressed(Input.Keys.DPAD_RIGHT);
            case UP:
                return Gdx.input.isKeyPressed(Input.Keys.UP)
                        || Gdx.input.isKeyPressed(Input.Keys.W)
                        || Gdx.input.isKeyPressed(Input.Keys.DPAD_UP);
            case DOWN:
                return Gdx.input.isKeyPressed(Input.Keys.DOWN)
                        || Gdx.input.isKeyPressed(Input.Keys.S)
                        || Gdx.input.isKeyPressed(Input.Keys.DPAD_DOWN);
            case JUMP:
                return Gdx.input.isKeyPressed(Input.Keys.SPACE)
                        || Gdx.input.isKeyPressed(Input.Keys.Z)
                        || Gdx.input.isKeyPressed(Input.Keys.BUTTON_A)
                        || Gdx.input.isKeyPressed(Input.Keys.BUTTON_X)
                        || Gdx.input.isKeyPressed(Input.Keys.BUTTON_R1);
            case RUN:
                return Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
                        || Gdx.input.isKeyPressed(Input.Keys.X)
                        || Gdx.input.isKeyPressed(Input.Keys.BUTTON_B)
                        || Gdx.input.isKeyPressed(Input.Keys.BUTTON_Y)
                        || Gdx.input.isKeyPressed(Input.Keys.BUTTON_L1)
                        || Gdx.input.isKeyPressed(Input.Keys.BUTTON_R2)
                        || Gdx.input.isKeyPressed(Input.Keys.BUTTON_L2);
            case START:
                return Gdx.input.isKeyPressed(Input.Keys.ENTER)
                        || Gdx.input.isKeyPressed(Input.Keys.BUTTON_START);
            case SELECT:
                return Gdx.input.isKeyPressed(Input.Keys.TAB)
                        || Gdx.input.isKeyPressed(Input.Keys.BUTTON_SELECT);
            case BACK:
                return Gdx.input.isKeyPressed(Input.Keys.ESCAPE)
                        || Gdx.input.isKeyPressed(Input.Keys.BACK)
                        || Gdx.input.isKeyPressed(Input.Keys.BUTTON_SELECT);
            default:
                return false;
        }
    }

    /** Desenha overlay nas barras pretas (chamar APOS present()). */
    public void drawTouchOverlayScreen(RenderContext render, BitmapFont font, LetterboxInfo box) {
        if (!touchPad.isActive()) return;
        render.beginScreenOverlay(box);
        touchPad.draw(render, font, box);
        render.endScreenOverlay();
    }

    public boolean isDown(Action a) {
        return current[a.ordinal()];
    }

    public boolean isPressed(Action a) {
        return current[a.ordinal()] && !previous[a.ordinal()];
    }

    public boolean isReleased(Action a) {
        return !current[a.ordinal()] && previous[a.ordinal()];
    }
}
