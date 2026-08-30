package com.fabioad.ddba.engine.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.fabioad.ddba.engine.renderer.LetterboxInfo;
import com.fabioad.ddba.engine.renderer.RenderContext;
import com.fabioad.ddba.engine.ui.TextUtil;

/**
 * VirtualGamepad
 * =============================================================================
 * Joypad virtual nas bordas (letterbox), com captura STICKY de ponteiros:
 * uma vez que o dedo pega D-pad / A / B, continua ativo ate soltar — mesmo se
 * escorregar um pouco. Isso permite segurar esq/dir + correr + pulo juntos.
 *
 * Correr (B) no touch e TOGGLE: toque rapido trava o run ate tocar de novo.
 * Teclado/joystick (GameInput) seguem hold normal e nao usam este latch.
 *
 * Layout pensado em 3 dedos (paisagem):
 *   - Esquerda: D-pad grande (L/R priorizados no platformer)
 *   - Direita: B (correr) e A (pulo) bem separados em diagonal Nintendo
 */
public final class VirtualGamepad {

    private enum Zone {
        NONE, DPAD, JUMP, RUN, START, BACK
    }

    private final boolean[] down = new boolean[GameInput.Action.values().length];
    private final Zone[] pointerZone = new Zone[20];

    /** Latch so de touch: RUN fica ativo ate o proximo toque em B. */
    private boolean runLatched;

    private boolean platformEnabled;
    private boolean visible = true;

    // Layout em pixels de tela (preenchido em layout())
    private float padCx, padCy, padArm, padThick, padDead;
    private float padHitR; // raio de captura do D-pad (maior que o visual)
    private float btnAx, btnAy, btnAr, btnAHit;
    private float btnBx, btnBy, btnBr, btnBHit;
    private float startX, startY, startW, startH;
    private float backX, backY, backW, backH;

    public VirtualGamepad() {
        for (int i = 0; i < pointerZone.length; i++) {
            pointerZone[i] = Zone.NONE;
        }
    }

    public void setPlatformEnabled(boolean enabled) {
        this.platformEnabled = enabled;
        if (!enabled) {
            clearPointers();
            resetTouchRunLatch();
        }
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        if (!visible) {
            clearPointers();
            resetTouchRunLatch();
        }
    }

    public boolean isActive() {
        return platformEnabled && visible;
    }

    public void update(LetterboxInfo box) {
        for (int i = 0; i < down.length; i++) {
            down[i] = false;
        }
        if (!isActive() || box == null) {
            clearPointers();
            runLatched = false;
            return;
        }

        layout(box);

        int max = Math.min(pointerZone.length, Math.max(10, Gdx.input.getMaxPointers()));
        for (int i = 0; i < max; i++) {
            if (!Gdx.input.isTouched(i)) {
                pointerZone[i] = Zone.NONE;
                continue;
            }
            float x = Gdx.input.getX(i);
            float y = box.touchToScreenY(Gdx.input.getY(i));

            // Captura no primeiro toque; mantem o papel ate soltar (sticky).
            if (pointerZone[i] == Zone.NONE) {
                Zone zone = assignZone(x, y);
                pointerZone[i] = zone;
                // Rising edge em B: toggle do run (toque rapido trava / destravar).
                if (zone == Zone.RUN) {
                    runLatched = !runLatched;
                }
            }
            applyZone(pointerZone[i], x, y);
        }

        // RUN no touch vem so do latch (nao precisa segurar o dedo).
        down[GameInput.Action.RUN.ordinal()] = runLatched;
    }

    public boolean isDown(GameInput.Action a) {
        return isActive() && down[a.ordinal()];
    }

    private void clearPointers() {
        for (int i = 0; i < pointerZone.length; i++) {
            pointerZone[i] = Zone.NONE;
        }
        for (int i = 0; i < down.length; i++) {
            down[i] = false;
        }
    }

    /** Ao esconder o pad (teclado/joy fisico), zera o toggle de correr. */
    private void resetTouchRunLatch() {
        runLatched = false;
        down[GameInput.Action.RUN.ordinal()] = false;
    }

    /**
     * Controles grandes e afastados o bastante para 2 dedos na direita
     * enquanto o polegar esquerdo segura o D-pad.
     */
    private void layout(LetterboxInfo box) {
        float unit = Math.max(34f, Math.min(box.winH, box.winW) * 0.072f);

        padArm = unit * 1.45f;
        padThick = unit * 1.25f;
        padDead = unit * 0.22f;
        padHitR = padArm + padThick * 0.85f;

        // A (pulo) maior; B (correr) um pouco menor, bem separado
        btnAr = unit * 1.40f;
        btnBr = unit * 1.20f;
        // Hit maior que o desenho, mas sem colidir A↔B (dois dedos)
        btnAHit = btnAr * 1.35f;
        btnBHit = btnBr * 1.35f;

        startW = unit * 3.2f;
        startH = unit * 0.85f;
        backW = unit * 1.6f;
        backH = startH;

        float leftBar = box.offsetX;
        float rightBar = box.winW - box.offsetX - box.drawW;
        float bottomBar = box.offsetY;

        // D-pad: baixo-esquerda, podendo invadir levemente o canvas
        float padHalf = padArm + padThick * 0.5f;
        if (leftBar > padHalf * 1.2f) {
            padCx = leftBar * 0.52f;
        } else {
            padCx = Math.max(padHalf + 6f, leftBar + padHalf * 0.15f);
        }
        padCy = bottomBar + Math.max(padHalf + 10f, Math.min(box.drawH * 0.28f, unit * 4.2f));

        // Cluster direito em diagonal Nintendo: B acima-esquerda, A abaixo-direita.
        // Distancia entre centros > soma dos raios de hit (cabem 2 dedos).
        float clusterGap = unit * 3.35f;
        if (rightBar > btnAr * 2.4f) {
            float mid = box.winW - rightBar * 0.40f;
            btnAx = mid + clusterGap * 0.42f;
            btnBx = mid - clusterGap * 0.50f;
        } else {
            btnAx = box.winW - Math.max(btnAr + 10f, rightBar * 0.12f + btnAr);
            btnBx = btnAx - clusterGap * 0.92f;
        }
        float baseY = bottomBar + Math.max(btnAr + 12f, Math.min(box.drawH * 0.26f, unit * 4.0f));
        btnAy = baseY;
        btnBy = baseY + clusterGap * 0.78f;

        // Evita B sair do topo da tela
        float maxBy = box.winH - btnBr - 8f;
        if (btnBy > maxBy) {
            float shift = btnBy - maxBy;
            btnBy -= shift;
            btnAy -= shift * 0.35f;
        }

        float topBar = box.winH - box.offsetY - box.drawH;
        float topY = box.offsetY + box.drawH + Math.max(4f, topBar * 0.25f);
        if (topY + startH > box.winH - 2f) {
            topY = box.winH - startH - 6f;
        }
        startX = box.winW * 0.5f - startW * 0.5f;
        startY = topY;
        backX = Math.max(6f, leftBar * 0.15f);
        backY = topY;
    }

    private Zone assignZone(float x, float y) {
        // Prioridade: menus; depois A/B pelo mais proximo; D-pad por ultimo.
        if (inRect(x, y, startX, startY, startW, startH)) {
            return Zone.START;
        }
        if (inRect(x, y, backX, backY, backW, backH)) {
            return Zone.BACK;
        }

        boolean hitA = inCircle(x, y, btnAx, btnAy, btnAHit);
        boolean hitB = inCircle(x, y, btnBx, btnBy, btnBHit);
        if (hitA && hitB) {
            float dA = dist2(x, y, btnAx, btnAy);
            float dB = dist2(x, y, btnBx, btnBy);
            return dA <= dB ? Zone.JUMP : Zone.RUN;
        }
        if (hitA) {
            return Zone.JUMP;
        }
        if (hitB) {
            return Zone.RUN;
        }
        if (inCircle(x, y, padCx, padCy, padHitR) || inDpadVisual(x, y)) {
            return Zone.DPAD;
        }
        // Faixa esquerda inferior: ainda captura como D-pad (facilita escorregar)
        if (x < boxLeftZone() && y < padCy + padHitR * 1.15f && y > padCy - padHitR * 1.4f) {
            return Zone.DPAD;
        }
        return Zone.NONE;
    }

    private static float dist2(float x, float y, float cx, float cy) {
        float dx = x - cx, dy = y - cy;
        return dx * dx + dy * dy;
    }

    private float boxLeftZone() {
        return Math.max(padCx + padHitR * 0.85f, Gdx.graphics.getWidth() * 0.42f);
    }

    private void applyZone(Zone zone, float x, float y) {
        switch (zone) {
            case START:
                down[GameInput.Action.START.ordinal()] = true;
                break;
            case BACK:
                down[GameInput.Action.BACK.ordinal()] = true;
                break;
            case JUMP:
                // Sticky: posicao apos o toque inicial nao importa
                down[GameInput.Action.JUMP.ordinal()] = true;
                break;
            case RUN:
                // RUN e latched em update(); toque so serve para o toggle no rising edge.
                break;
            case DPAD:
                applyDpad(x, y);
                break;
            case NONE:
            default:
                break;
        }
    }

    private void applyDpad(float x, float y) {
        float dx = x - padCx;
        float dy = y - padCy;
        if (Math.abs(dx) < padDead && Math.abs(dy) < padDead) {
            return;
        }

        // Platformer: favorece esquerda/direita (gate vertical mais estreito).
        float horizBias = 1.15f;
        if (Math.abs(dx) * horizBias >= Math.abs(dy)) {
            if (dx < 0) {
                down[GameInput.Action.LEFT.ordinal()] = true;
            } else {
                down[GameInput.Action.RIGHT.ordinal()] = true;
            }
        } else {
            if (dy > 0) {
                down[GameInput.Action.UP.ordinal()] = true;
            } else {
                down[GameInput.Action.DOWN.ordinal()] = true;
            }
        }
    }

    private boolean inDpadVisual(float x, float y) {
        float halfT = padThick * 0.5f;
        float hx = padCx - padArm - halfT;
        float hy = padCy - halfT;
        float hw = (padArm + halfT) * 2f;
        float hh = padThick;
        float vx = padCx - halfT;
        float vy = padCy - padArm - halfT;
        float vw = padThick;
        float vh = (padArm + halfT) * 2f;
        return inRect(x, y, hx, hy, hw, hh) || inRect(x, y, vx, vy, vw, vh);
    }

    private static boolean inRect(float x, float y, float rx, float ry, float rw, float rh) {
        return x >= rx && x <= rx + rw && y >= ry && y <= ry + rh;
    }

    private static boolean inCircle(float x, float y, float cx, float cy, float r) {
        float dx = x - cx, dy = y - cy;
        return dx * dx + dy * dy <= r * r;
    }

    public void draw(RenderContext render, BitmapFont font, LetterboxInfo box) {
        if (!isActive() || box == null) {
            return;
        }
        layout(box);

        float halfT = padThick * 0.5f;
        float aEdge = 0.30f;
        float aFill = 0.18f;
        float aHot = 0.42f;

        drawPadArm(render, padCx - padArm - halfT, padCy - halfT,
                (padArm + halfT) * 2f, padThick,
                down[GameInput.Action.LEFT.ordinal()] || down[GameInput.Action.RIGHT.ordinal()],
                aEdge, aFill, aHot);
        drawPadArm(render, padCx - halfT, padCy - padArm - halfT,
                padThick, (padArm + halfT) * 2f,
                down[GameInput.Action.UP.ordinal()] || down[GameInput.Action.DOWN.ordinal()],
                aEdge, aFill, aHot);

        render.fillRect(padCx - halfT, padCy - halfT, padThick, padThick,
                new Color(1f, 1f, 1f, 0.22f));

        drawDot(render, padCx - padArm + halfT * 0.3f, padCy,
                down[GameInput.Action.LEFT.ordinal()]);
        drawDot(render, padCx + padArm - halfT * 0.3f, padCy,
                down[GameInput.Action.RIGHT.ordinal()]);
        drawDot(render, padCx, padCy + padArm - halfT * 0.3f,
                down[GameInput.Action.UP.ordinal()]);
        drawDot(render, padCx, padCy - padArm + halfT * 0.3f,
                down[GameInput.Action.DOWN.ordinal()]);

        drawRoundButton(render, btnBx, btnBy, btnBr,
                down[GameInput.Action.RUN.ordinal()],
                new Color(0.95f, 0.75f, 0.25f, 0.32f));
        drawRoundButton(render, btnAx, btnAy, btnAr,
                down[GameInput.Action.JUMP.ordinal()],
                new Color(0.35f, 0.85f, 0.45f, 0.32f));

        if (font != null) {
            Color label = new Color(1f, 1f, 1f, 0.50f);
            Color labelHot = new Color(1f, 1f, 0.7f, 0.80f);
            TextUtil.drawCentered(render.getBatch(), font, "B", btnBx, btnBy + 5f, 0.5f,
                    down[GameInput.Action.RUN.ordinal()] ? labelHot : label);
            TextUtil.drawCentered(render.getBatch(), font, "A", btnAx, btnAy + 5f, 0.5f,
                    down[GameInput.Action.JUMP.ordinal()] ? labelHot : label);
        }

        Color startCol = down[GameInput.Action.START.ordinal()]
                ? new Color(1f, 1f, 0.6f, 0.40f) : new Color(1f, 1f, 1f, 0.18f);
        render.fillRect(startX, startY, startW, startH, startCol);
        Color backCol = down[GameInput.Action.BACK.ordinal()]
                ? new Color(1f, 0.55f, 0.55f, 0.40f) : new Color(1f, 0.8f, 0.8f, 0.16f);
        render.fillRect(backX, backY, backW, backH, backCol);

        if (font != null) {
            Color t = new Color(1f, 1f, 1f, 0.50f);
            TextUtil.drawCentered(render.getBatch(), font, "START",
                    startX + startW / 2f, startY + startH * 0.72f, 0.5f, t);
            TextUtil.drawCentered(render.getBatch(), font, "ESC",
                    backX + backW / 2f, backY + backH * 0.72f, 0.5f, t);
        }
    }

    private static void drawPadArm(RenderContext render, float x, float y, float w, float h,
                                   boolean hot, float aEdge, float aFill, float aHot) {
        Color edge = hot ? new Color(1f, 1f, 0.5f, aHot) : new Color(1f, 1f, 1f, aEdge);
        Color fill = hot ? new Color(1f, 0.95f, 0.4f, aFill + 0.12f) : new Color(1f, 1f, 1f, aFill);
        render.fillRect(x, y, w, h, edge);
        if (w > 4 && h > 4) {
            render.fillRect(x + 1, y + 1, w - 2, h - 2, fill);
        }
    }

    private static void drawDot(RenderContext render, float cx, float cy, boolean hot) {
        float s = hot ? 8f : 6f;
        Color c = hot ? new Color(1f, 1f, 0.4f, 0.55f) : new Color(1f, 1f, 1f, 0.30f);
        render.fillRect(cx - s * 0.5f, cy - s * 0.5f, s, s, c);
    }

    private static void drawRoundButton(RenderContext render, float cx, float cy, float r,
                                        boolean hot, Color base) {
        float alpha = hot ? Math.min(0.58f, base.a + 0.22f) : base.a;
        Color ring = new Color(1f, 1f, 1f, hot ? 0.48f : 0.24f);
        Color fill = new Color(base.r, base.g, base.b, alpha);
        float d = r * 2f;
        render.fillRect(cx - r, cy - r, d, d, ring);
        render.fillRect(cx - r + 2, cy - r + 2, d - 4, d - 4, fill);
        render.fillRect(cx - r * 0.55f, cy - r - 1, r * 1.1f, d + 2, fill);
        render.fillRect(cx - r - 1, cy - r * 0.55f, d + 2, r * 1.1f, fill);
    }
}
