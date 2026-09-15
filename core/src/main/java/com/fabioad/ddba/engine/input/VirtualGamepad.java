package com.fabioad.ddba.engine.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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

    // Texturas procedurais em alta resolucao: ficam nitidas mesmo em celulares
    // com densidade alta, sem alterar a pixel font usada no restante do jogo.
    private final Texture dpadTexture;
    private final Texture circleTexture;
    private final Texture pillTexture;

    public VirtualGamepad() {
        for (int i = 0; i < pointerZone.length; i++) {
            pointerZone[i] = Zone.NONE;
        }
        dpadTexture = createDpadTexture();
        circleTexture = createCircleTexture();
        pillTexture = createPillTexture();
    }

    public void dispose() {
        dpadTexture.dispose();
        circleTexture.dispose();
        pillTexture.dispose();
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

        drawDpad(render);

        drawRoundButton(render, btnBx, btnBy, btnBr,
                down[GameInput.Action.RUN.ordinal()],
                new Color(0.95f, 0.55f, 0.16f, 0.78f));
        drawRoundButton(render, btnAx, btnAy, btnAr,
                down[GameInput.Action.JUMP.ordinal()],
                new Color(0.16f, 0.78f, 0.68f, 0.78f));

        if (font != null) {
            Color label = new Color(1f, 1f, 1f, 0.72f);
            Color labelHot = new Color(1f, 1f, 0.82f, 0.98f);
            TextUtil.drawCentered(render.getBatch(), font, "B", btnBx, btnBy + 7f, 1f,
                    down[GameInput.Action.RUN.ordinal()] ? labelHot : label);
            TextUtil.drawCentered(render.getBatch(), font, "A", btnAx, btnAy + 7f, 1f,
                    down[GameInput.Action.JUMP.ordinal()] ? labelHot : label);
        }

        Color startCol = down[GameInput.Action.START.ordinal()]
                ? new Color(0.35f, 0.95f, 0.85f, 0.90f)
                : new Color(0.12f, 0.35f, 0.42f, 0.82f);
        drawPill(render, startX, startY, startW, startH, startCol);
        Color backCol = down[GameInput.Action.BACK.ordinal()]
                ? new Color(1f, 0.35f, 0.40f, 0.90f)
                : new Color(0.40f, 0.16f, 0.24f, 0.82f);
        drawPill(render, backX, backY, backW, backH, backCol);

        if (font != null) {
            Color t = new Color(1f, 1f, 1f, 0.74f);
            TextUtil.drawCentered(render.getBatch(), font, "START",
                    startX + startW / 2f, startY + startH * 0.72f, 1f, t);
            TextUtil.drawCentered(render.getBatch(), font, "ESC",
                    backX + backW / 2f, backY + backH * 0.72f, 1f, t);
        }
    }

    private void drawDpad(RenderContext render) {
        float radius = padArm + padThick * 0.5f;
        drawTexture(render, dpadTexture, padCx - radius + 4f, padCy - radius - 6f,
                radius * 2f, radius * 2f, new Color(0f, 0f, 0f, 0.32f));
        drawTexture(render, dpadTexture, padCx - radius, padCy - radius,
                radius * 2f, radius * 2f, new Color(0.10f, 0.35f, 0.43f, 0.90f));

        float highlight = Math.max(18f, padThick * 0.48f);
        Color hot = new Color(0.25f, 0.95f, 0.80f, 0.92f);
        if (down[GameInput.Action.LEFT.ordinal()]) {
            drawTexture(render, circleTexture, padCx - padArm - highlight * 0.5f,
                    padCy - highlight * 0.5f, highlight, highlight, hot);
        }
        if (down[GameInput.Action.RIGHT.ordinal()]) {
            drawTexture(render, circleTexture, padCx + padArm - highlight * 0.5f,
                    padCy - highlight * 0.5f, highlight, highlight, hot);
        }
        if (down[GameInput.Action.UP.ordinal()]) {
            drawTexture(render, circleTexture, padCx - highlight * 0.5f,
                    padCy + padArm - highlight * 0.5f, highlight, highlight, hot);
        }
        if (down[GameInput.Action.DOWN.ordinal()]) {
            drawTexture(render, circleTexture, padCx - highlight * 0.5f,
                    padCy - padArm - highlight * 0.5f, highlight, highlight, hot);
        }
    }

    private void drawRoundButton(RenderContext render, float cx, float cy, float r,
                                 boolean hot, Color base) {
        float size = r * 2f * (hot ? 1.06f : 1f);
        float x = cx - size * 0.5f;
        float y = cy - size * 0.5f;
        drawTexture(render, circleTexture, x + 4f, y - 6f, size, size,
                new Color(0f, 0f, 0f, 0.35f));
        drawTexture(render, circleTexture, x, y, size, size,
                new Color(base.r, base.g, base.b, hot ? 0.98f : base.a));
        if (hot) {
            drawTexture(render, circleTexture, x + size * 0.14f, y + size * 0.14f,
                    size * 0.72f, size * 0.72f, new Color(1f, 1f, 1f, 0.12f));
        }
    }

    private void drawPill(RenderContext render, float x, float y, float w, float h, Color color) {
        drawTexture(render, pillTexture, x + 3f, y - 4f, w, h,
                new Color(0f, 0f, 0f, 0.35f));
        drawTexture(render, pillTexture, x, y, w, h, color);
    }

    private static void drawTexture(RenderContext render, Texture texture,
                                    float x, float y, float w, float h, Color color) {
        SpriteBatch batch = render.getBatch();
        batch.setColor(color);
        batch.draw(texture, x, y, w, h);
        batch.setColor(Color.WHITE);
    }

    private static Texture createCircleTexture() {
        int size = 256;
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        float center = (size - 1) * 0.5f;
        float outer = center - 1f;
        float inner = center - 10f;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = x - center;
                float dy = y - center;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);
                float alpha = distance <= inner ? 1f : distance <= outer ? 0.82f : 0f;
                if (alpha > 0f) {
                    pm.setColor(1f, 1f, 1f, alpha);
                    pm.drawPixel(x, y);
                }
            }
        }
        return textureFrom(pm);
    }

    private static Texture createDpadTexture() {
        int size = 512;
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        float center = (size - 1) * 0.5f;
        float radius = size * 0.48f;
        int arm = Math.round(size * 0.20f);
        int reach = Math.round(size * 0.37f);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = x - center;
                float dy = y - center;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);
                boolean body = distance <= radius
                        && (Math.abs(dx) <= reach && Math.abs(dy) <= arm
                        || Math.abs(dy) <= reach && Math.abs(dx) <= arm);
                if (body) {
                    float alpha = distance > radius - 8f ? 0.62f : 1f;
                    pm.setColor(1f, 1f, 1f, alpha);
                    pm.drawPixel(x, y);
                }
            }
        }
        return textureFrom(pm);
    }

    private static Texture createPillTexture() {
        int width = 512;
        int height = 160;
        Pixmap pm = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        int radius = height / 2 - 2;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (inRoundedRect(x, y, width, height, radius, 0)) {
                    pm.setColor(1f, 1f, 1f, 1f);
                    pm.drawPixel(x, y);
                }
            }
        }
        return textureFrom(pm);
    }

    private static boolean inRoundedRect(int x, int y, int width, int height,
                                         int radius, int inset) {
        float left = inset;
        float right = width - inset - 1f;
        float bottom = inset;
        float top = height - inset - 1f;
        if (x < left || x > right || y < bottom || y > top) {
            return false;
        }
        if (x >= left + radius && x <= right - radius
                || y >= bottom + radius && y <= top - radius) {
            return true;
        }
        float cx = x < left + radius ? left + radius : right - radius;
        float cy = y < bottom + radius ? bottom + radius : top - radius;
        float dx = x - cx;
        float dy = y - cy;
        return dx * dx + dy * dy <= radius * radius;
    }

    private static Texture textureFrom(Pixmap pm) {
        Texture texture = new Texture(pm);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pm.dispose();
        return texture;
    }
}
