package com.danieldobolo.adventure;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.io.IOException;

/**
 * Real-time Canvas version of the trailer. It deliberately avoids a video
 * player: the device draws the camera move, text, pixel dissolve and fades
 * from the small source assets on every 30fps animation step.
 */
public final class DanielTrailerView extends View {
    private static final float LOGICAL_W = 1920f;
    private static final float LOGICAL_H = 1080f;
    private static final long FPS = 30L;
    private static final long FRAME_MS = 1000L / FPS;
    private static final long INTRO_MS = 9500L;
    private static final long CHARACTER_MS = 32000L;
    private static final long END_CARD_1_MS = 12000L;
    private static final long END_CARD_2_MS = 15000L;
    private static final long TAIL_MS = 3077L;
    private static final long TOTAL_MS = INTRO_MS + 6L * CHARACTER_MS
            + END_CARD_1_MS + END_CARD_2_MS + TAIL_MS;

    private static final String[] NAMES = {
            "DANIEL DO BOLO", "LEO", "TUFÃO", "MARCOS", "VICENTE", "ARAPINHA"
    };
    private static final String[][] COPY = {
            {"Morador do bairro de Brasília Teimosa há", "quase 60 anos, é dono da",
                    "barraca mais badalada da região. Quem", "“sobrevive” à sua famosa",
                    "misturada aguenta tudo e todos."},
            {"Vizinho e cliente de Daniel, é um jovem que", "adora encontrar a galera lá na barraca. Mas,",
                    "quando bebe a misturada, fica tão doidão que", "precisa de ajuda para voltar para casa."},
            {"Vizinho e cliente de Daniel há anos, é um dos", "maiores magnatas dos videogames de Recife e",
                    "também o maior conquistador de mulheres da", "região.", "",
                    "Cuidado: sem você perceber, Tufão já deve ter", "se apaixonado, ficado, engravidado e se", "separado de você."},
            {"Cliente de Daniel que ama viajar. Já conheceu", "todo Pernambuco, o Brasil, Portugal, Estados",
                    "Unidos, França e Itália.", "", "Em breve vai conhecer até a Lua, pois já está",
                    "com o ingresso da Starship da SpaceX."},
            {"Policial e amigo de Daniel, faz a segurança da", "galera...", "", "Até tomar a misturada de Daniel.", "",
                    "Depois disso, nem a galera consegue segurar", "Vicente."},
            {"Morador de Fernando de Noronha, vive navegando", "pelo mar em barcos e lanchas.", "",
                    "Adora arretar Daniel falando mal da cerveja dele,", "e Daniel sempre responde dizendo que Arapinha é",
                    "um “arregão de gente rica” e outras coisas mais.", "", "Mas, no final, todo mundo entra na greia."}
    };

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Bitmap fundo;
    private final Bitmap final01;
    private final Bitmap final02;
    private final Bitmap[][] art = new Bitmap[6][3];
    private ValueAnimator animator;
    private long lastFrame = -1L;

    public DanielTrailerView(Context context) {
        super(context);
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
        AssetManager assets = context.getAssets();
        try {
            fundo = load(assets, "fundo.webp");
            final01 = load(assets, "final01.webp");
            final02 = load(assets, "final02.webp");
            String[] keys = {"daniel", "leo", "tufao", "marcos", "vicente", "arapinha"};
            for (int i = 0; i < keys.length; i++) {
                art[i][0] = load(assets, keys[i] + "_pixel.webp");
                art[i][1] = load(assets, keys[i] + "_desenho.webp");
                art[i][2] = load(assets, keys[i] + "_humano.webp");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Trailer assets missing", e);
        }
        text.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        setFocusable(true);
    }

    private static Bitmap load(AssetManager assets, String name) throws IOException {
        try (java.io.InputStream input = assets.open(name)) {
            return BitmapFactory.decodeStream(input);
        }
    }

    public void start() {
        if (animator == null) {
            animator = ValueAnimator.ofInt(0, (int) TOTAL_MS);
            animator.setDuration(TOTAL_MS);
            animator.setInterpolator(new LinearInterpolator());
            animator.addUpdateListener(valueAnimator -> {
                long frame = valueAnimator.getCurrentPlayTime() / FRAME_MS;
                if (frame != lastFrame) {
                    lastFrame = frame;
                    invalidate();
                }
            });
        }
        animator.start();
    }

    public void pause() {
        if (animator != null && animator.isRunning()) {
            animator.pause();
        }
    }

    public void resume() {
        if (animator != null && animator.isPaused()) {
            animator.resume();
        }
    }

    public void stop() {
        if (animator != null) {
            animator.cancel();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        long time = animator == null ? 0L : Math.min(TOTAL_MS, animator.getCurrentPlayTime());
        canvas.drawColor(Color.BLACK);
        float scale = Math.min(getWidth() / LOGICAL_W, getHeight() / LOGICAL_H);
        float left = (getWidth() - LOGICAL_W * scale) / 2f;
        float top = (getHeight() - LOGICAL_H * scale) / 2f;
        canvas.save();
        canvas.translate(left, top);
        canvas.scale(scale, scale);

        if (time < INTRO_MS) {
            drawIntro(canvas, time);
        } else if (time < INTRO_MS + 6L * CHARACTER_MS) {
            long relative = time - INTRO_MS;
            int index = Math.min(5, (int) (relative / CHARACTER_MS));
            drawCharacter(canvas, index, relative - index * CHARACTER_MS);
        } else if (time < INTRO_MS + 6L * CHARACTER_MS + END_CARD_1_MS) {
            drawCard(canvas, final01, time - INTRO_MS - 6L * CHARACTER_MS, END_CARD_1_MS, true);
        } else if (time < INTRO_MS + 6L * CHARACTER_MS + END_CARD_1_MS + END_CARD_2_MS) {
            drawCard(canvas, final02, time - INTRO_MS - 6L * CHARACTER_MS - END_CARD_1_MS, END_CARD_2_MS, false);
        }
        canvas.restore();
    }

    private void drawIntro(Canvas c, long time) {
        float seconds = time / 1000f;
        float progress = clamp(seconds / 6.5f);
        float zoom = 1f + 4f * (1f - easeOut(progress));
        drawCover(c, fundo, new RectF(0, 0, LOGICAL_W, LOGICAL_H), zoom);
        if (seconds > 8.25f) {
            float fade = clamp((seconds - 8.25f) / 1.25f);
            fill(c, Color.argb((int) (255 * fade), 2, 7, 18));
            drawPixelBlocks(c, fade, (int) (seconds * FPS));
        }
        if (seconds < .6f) {
            fill(c, Color.argb((int) (200 * (1f - seconds / .6f)), 0, 0, 0));
        }
    }

    private void drawCharacter(Canvas c, int index, long localTime) {
        float seconds = localTime / 1000f;
        drawGameBackground(c);
        roundPanel(c, new RectF(52, 112, 858, 950), 30, Color.argb(148, 7, 10, 28), Color.argb(150, 50, 216, 211));
        roundPanel(c, new RectF(916, 152, 1840, 950), 30, Color.argb(186, 6, 12, 29), Color.argb(165, 243, 184, 68));
        fillRect(c, new RectF(918, 248, 928, 830), Color.argb(215, 243, 184, 68));
        drawLabel(c, "DANIEL DO BOLO'S ADVENTURE", 78, 44, 28, Color.rgb(255, 219, 107));
        drawLabelRight(c, "PERSONAGEM " + String.format("%02d", index + 1) + " / 06", 1810, 47, 22, Color.rgb(112, 231, 226));
        drawLabelCentered(c, "PIXEL ART  •  DESENHO  •  HUMANO", 455, 890, 18, Color.rgb(165, 211, 217));

        long frame = (long) (seconds * FPS);
        if (seconds < 6f) {
            drawFitted(c, art[index][0], new RectF(70, 145, 790, 965), 1f, 0, 0);
        } else if (seconds < 14f) {
            drawMorph(c, art[index][0], art[index][1], clamp((seconds - 6f) / 8f), frame);
        } else if (seconds < 18f) {
            drawFitted(c, art[index][1], new RectF(70, 145, 790, 965), 1f, 0, 0);
        } else if (seconds < 26f) {
            drawMorph(c, art[index][1], art[index][2], clamp((seconds - 18f) / 8f), frame);
        } else {
            drawFitted(c, art[index][2], new RectF(70, 145, 790, 965), 1f, 0, 0);
        }

        float entrance = easeOut(clamp((seconds - .15f) / 1.45f));
        float offset = (1f - entrance) * -245f;
        drawLabel(c, NAMES[index], 980, 206 + offset, 66, Color.rgb(255, 207, 77));
        fillRect(c, new RectF(980, 295 + offset, 1800, 298 + offset), Color.rgb(62, 222, 212));
        float y = 330 + offset;
        for (String line : COPY[index]) {
            if (!line.isEmpty()) {
                drawLabel(c, line, 980, y, 34, Color.rgb(238, 246, 248));
            }
            y += 49;
        }

        if (seconds < .85f) {
            fill(c, Color.argb((int) (230 * (1f - easeOut(seconds / .85f))), 0, 0, 0));
        } else if (seconds > 31.1f) {
            float fade = clamp((seconds - 31.1f) / .9f);
            fill(c, Color.argb((int) (245 * fade), 0, 0, 0));
            drawPixelBlocks(c, fade, (int) frame);
        }
    }

    private void drawMorph(Canvas c, Bitmap first, Bitmap second, float progress, long frame) {
        RectF box = new RectF(70, 145, 790, 965);
        float breathe = 1f + .035f * (float) Math.sin(Math.PI * progress);
        drawFitted(c, first, box, 1f + .018f * (float) Math.sin(Math.PI * progress), 0, 0);
        if (progress >= .97f) {
            drawFitted(c, second, box, breathe, 0, 0);
            return;
        }
        if (progress <= .03f) {
            return;
        }
        RectF secondRect = fittedRect(second, box, breathe, 0, 0);
        int cols = 12;
        int rows = 14;
        float cellW = box.width() / cols;
        float cellH = box.height() / rows;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                float hash = cellHash(col, row, frame);
                float moving = .06f * (float) Math.sin(frame * .11f + col * .7f);
                if (hash + moving < progress) {
                    c.save();
                    c.clipRect(box.left + col * cellW, box.top + row * cellH,
                            box.left + (col + 1) * cellW, box.top + (row + 1) * cellH);
                    c.drawBitmap(second, null, secondRect, paint);
                    c.restore();
                }
            }
        }
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(Color.argb(130, 77, 244, 226));
        for (int col = 0; col < cols; col++) {
            if (Math.abs(cellHash(col, 6, frame) - progress) < .045f) {
                c.drawRect(box.left + col * cellW, box.top, box.left + (col + 1) * cellW, box.bottom, paint);
            }
        }
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawCard(Canvas c, Bitmap source, long localTime, long duration, boolean cartridge) {
        float seconds = localTime / 1000f;
        drawCover(c, source, new RectF(0, 0, LOGICAL_W, LOGICAL_H), 1.08f);
        fill(c, Color.argb(56, 4, 8, 22));
        drawFitted(c, source, new RectF(85, 60, 1835, 1020), 1f + .025f * easeOut(clamp(seconds / 1000f)), 0, 0);
        if (cartridge) {
            drawLabelCentered(c, "SE VOCÊ É FÃ DE RETRO GAME,", 960, 120, 45, Color.rgb(255, 226, 114));
            drawLabelCentered(c, "ADQUIRA A VERSÃO EM CARTUCHO", 960, 192, 48, Color.rgb(255, 226, 114));
            drawLabelCentered(c, "PARA SUPER NINTENDO!", 960, 264, 48, Color.rgb(255, 226, 114));
        } else {
            drawLabelCentered(c, "DANIEL DO BOLO'S ADVENTURE", 960, 130, 60, Color.rgb(255, 225, 106));
            drawLabelCentered(c, "DISPONÍVEL NA GOOGLE PLAY", 960, 220, 47, Color.WHITE);
        }
        drawLabelCentered(c, "fabioad.com.br/danieldobolo", 960, 900, 50, Color.rgb(111, 247, 227));
        float alpha = 1f;
        if (seconds < .9f) alpha *= easeOut(clamp(seconds / .9f));
        if (!cartridge && seconds > (duration / 1000f - 3f)) {
            alpha *= clamp((duration / 1000f - seconds) / 3f);
        }
        if (alpha < .999f) {
            fill(c, Color.argb((int) (255f * (1f - alpha)), 0, 0, 0));
        }
    }

    private void drawGameBackground(Canvas c) {
        paint.setShader(new LinearGradient(0, 0, 0, LOGICAL_H,
                Color.rgb(14, 15, 38), Color.rgb(8, 47, 74), Shader.TileMode.CLAMP));
        c.drawRect(0, 0, LOGICAL_W, LOGICAL_H, paint);
        paint.setShader(null);
        paint.setStrokeWidth(1);
        paint.setColor(Color.argb(26, 90, 178, 207));
        for (int y = 0; y < LOGICAL_H; y += 54) c.drawLine(0, y, LOGICAL_W, y, paint);
        paint.setColor(Color.argb(18, 72, 123, 182));
        for (int x = 0; x < LOGICAL_W; x += 96) c.drawLine(x, 0, x, LOGICAL_H, paint);
        fillRect(c, new RectF(0, 0, LOGICAL_W, 12), Color.rgb(243, 186, 66));
        fillRect(c, new RectF(0, LOGICAL_H - 12, LOGICAL_W, LOGICAL_H), Color.rgb(14, 199, 190));
    }

    private void drawFitted(Canvas c, Bitmap bitmap, RectF box, float scale, float dx, float dy) {
        c.drawBitmap(bitmap, null, fittedRect(bitmap, box, scale, dx, dy), paint);
    }

    private static RectF fittedRect(Bitmap bitmap, RectF box, float scale, float dx, float dy) {
        float factor = Math.min(box.width() / bitmap.getWidth(), box.height() / bitmap.getHeight()) * scale;
        float w = bitmap.getWidth() * factor;
        float h = bitmap.getHeight() * factor;
        float x = box.centerX() - w / 2f + dx;
        float y = box.centerY() - h / 2f + dy;
        return new RectF(x, y, x + w, y + h);
    }

    private static void drawCover(Canvas c, Bitmap bitmap, RectF box, float zoom) {
        float factor = Math.max(box.width() / bitmap.getWidth(), box.height() / bitmap.getHeight()) * zoom;
        float w = bitmap.getWidth() * factor;
        float h = bitmap.getHeight() * factor;
        RectF dst = new RectF(box.centerX() - w / 2f, box.centerY() - h / 2f,
                box.centerX() + w / 2f, box.centerY() + h / 2f);
        c.save();
        c.clipRect(box);
        c.drawBitmap(bitmap, null, dst, new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        c.restore();
    }

    private void roundPanel(Canvas c, RectF r, float radius, int fill, int stroke) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(fill);
        c.drawRoundRect(r, radius, radius, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(stroke);
        c.drawRoundRect(r, radius, radius, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawLabel(Canvas c, String value, float x, float y, float size, int color) {
        drawLabelInternal(c, value, x, y, size, color, Paint.Align.LEFT);
    }

    private void drawLabelRight(Canvas c, String value, float x, float y, float size, int color) {
        drawLabelInternal(c, value, x, y, size, color, Paint.Align.RIGHT);
    }

    private void drawLabelCentered(Canvas c, String value, float x, float y, float size, int color) {
        drawLabelInternal(c, value, x, y, size, color, Paint.Align.CENTER);
    }

    private void drawLabelInternal(Canvas c, String value, float x, float y, float size, int color, Paint.Align align) {
        text.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        text.setTextSize(size);
        text.setTextAlign(align);
        text.setStyle(Paint.Style.FILL);
        text.setColor(Color.BLACK);
        c.drawText(value, x + 6, y + 7, text);
        text.setColor(color);
        c.drawText(value, x, y, text);
    }

    private void fill(Canvas c, int color) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        c.drawRect(0, 0, LOGICAL_W, LOGICAL_H, paint);
    }

    private void fillRect(Canvas c, RectF r, int color) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        c.drawRect(r, paint);
    }

    private void drawPixelBlocks(Canvas c, float amount, int frame) {
        int count = (int) (42 * clamp(amount));
        for (int i = 0; i < count; i++) {
            int seed = i * 1103515245 + frame * 12345;
            int x = Math.abs(seed) % 120 * 16;
            int y = Math.abs(seed / 97) % 68 * 16;
            int size = 16 + (Math.abs(seed / 31) % 3) * 16;
            paint.setColor(Color.argb((int) (170 * amount), (i % 3 == 0) ? 244 : 58,
                    (i % 3 == 0) ? 190 : 224, (i % 3 == 0) ? 63 : 214));
            c.drawRect(x, y, Math.min(LOGICAL_W, x + size), Math.min(LOGICAL_H, y + size), paint);
        }
    }

    private static float cellHash(int col, int row, long frame) {
        long value = col * 92837111L + row * 689287499L + frame * 31L;
        value = (value ^ (value >>> 16)) * 0x45d9f3bL;
        value = (value ^ (value >>> 16)) * 0x45d9f3bL;
        value = value ^ (value >>> 16);
        return (value & 0x7fffffffL) / (float) 0x7fffffffL;
    }

    private static float clamp(float value) {
        return Math.max(0f, Math.min(1f, value));
    }

    private static float easeOut(float value) {
        value = clamp(value);
        return 1f - (float) Math.pow(1f - value, 3);
    }
}
