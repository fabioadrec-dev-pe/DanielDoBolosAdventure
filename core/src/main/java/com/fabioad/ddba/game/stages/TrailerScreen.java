package com.fabioad.ddba.game.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.fabioad.ddba.engine.assets.AssetPaths;
import com.fabioad.ddba.engine.core.GameContext;
import com.fabioad.ddba.engine.input.GameInput;
import com.fabioad.ddba.game.core.DanielGame;

import java.util.ArrayList;
import java.util.List;

/**
 * Abertura em tempo real, renderizada em viewport 16:9 independente da
 * resolucao retro usada pelo restante do jogo. Em uma tela menor, o viewport
 * e reduzido proporcionalmente e recebe barras pretas, sem distorcer a cena.
 */
public final class TrailerScreen extends ScreenAdapter {

    private static final float WIDTH = 1920f;
    private static final float HEIGHT = 1080f;
    private static final float INTRO_SECONDS = 9.5f;
    private static final float CHARACTER_SECONDS = 32f;
    private static final float END_CARD_1_SECONDS = 12f;
    private static final float END_CARD_2_SECONDS = 15f;
    private static final float TAIL_SECONDS = 3.077f;
    private static final float TOTAL_SECONDS = INTRO_SECONDS + 6f * CHARACTER_SECONDS
            + END_CARD_1_SECONDS + END_CARD_2_SECONDS + TAIL_SECONDS;

    private static final float CHARACTER_TITLE_SIZE = 78f;
    private static final float CHARACTER_TEXT_SIZE = 40f;
    private static final float TEXT_MAX_WIDTH = 780f;
    private static final float TEXT_BOTTOM = 850f;

    private static final String[] KEYS = {
            "daniel", "leo", "tufao", "marcos", "vicente", "arapinha"
    };
    private static final String[] NAMES = {
            "DANIEL DO BOLO", "LEO", "TUFAO", "MARCOS", "VICENTE", "ARAPINHA"
    };
    private static final String[][] COPY = {
            {"Morador do bairro de Brasilia Teimosa ha", "quase 60 anos, e dono da",
                    "barraca mais badalada da regiao. Quem", "sobrevive a sua famosa",
                    "misturada aguenta tudo e todos."},
            {"Vizinho e cliente de Daniel, e um jovem que", "adora encontrar a galera la na barraca. Mas,",
                    "quando bebe a misturada, fica tao doidao que", "precisa de ajuda para voltar para casa."},
            {"Vizinho e cliente de Daniel ha anos, e um dos", "maiores magnatas dos videogames de Recife e",
                    "tambem o maior conquistador de mulheres da", "regiao.", "",
                    "Cuidado: sem voce perceber, Tufao ja deve ter", "se apaixonado, ficado, engravidado e se",
                    "separado de voce."},
            {"Cliente de Daniel que ama viajar. Ja conheceu", "todo Pernambuco, o Brasil, Portugal, Estados",
                    "Unidos, Franca e Italia.", "", "Em breve vai conhecer ate a Lua, pois ja esta",
                    "com o ingresso da Starship da SpaceX."},
            {"Policial e amigo de Daniel, faz a seguranca da", "galera...", "", "Ate tomar a misturada de Daniel.", "",
                    "Depois disso, nem a galera consegue segurar", "Vicente."},
            {"Morador de Fernando de Noronha, vive navegando", "pelo mar em barcos e lanchas.", "",
                    "Adora arretar Daniel falando mal da cerveja dele,", "e Daniel sempre responde dizendo que Arapinha e",
                    "um arregao de gente rica e outras coisas mais.", "", "Mas, no final, todo mundo entra na greia."}
    };

    private final DanielGame game;
    private final GameContext ctx;
    private final SpriteBatch batch = new SpriteBatch();
    private final OrthographicCamera camera = new OrthographicCamera(WIDTH, HEIGHT);
    private final Texture whitePixel = createWhitePixel();
    private final GlyphLayout layout = new GlyphLayout();
    private final Texture fundo;
    private final Texture final01;
    private final Texture final02;
    private final Texture[][] art = new Texture[6][3];

    private float time;
    private boolean finished;

    public TrailerScreen(DanielGame game) {
        this.game = game;
        this.ctx = game.getContext();
        fundo = load("trailer/fundo.png");
        final01 = load("trailer/final01.png");
        final02 = load("trailer/final02.png");
        for (int i = 0; i < KEYS.length; i++) {
            art[i][0] = load("trailer/" + KEYS[i] + "_pixel.png");
            art[i][1] = load("trailer/" + KEYS[i] + "_desenho.png");
            art[i][2] = load("trailer/" + KEYS[i] + "_humano.png");
        }
    }

    @Override
    public void show() {
        ctx.audio.playMusic(AssetPaths.MUSIC_TRAILER, false);
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = WIDTH;
        camera.viewportHeight = HEIGHT;
        camera.position.set(WIDTH / 2f, HEIGHT / 2f, 0f);
        camera.update();
    }

    @Override
    public void render(float delta) {
        ctx.input.update();
        if (!finished) {
            if (anyInputPressed()) {
                finish();
                return;
            } else {
                time = Math.min(TOTAL_SECONDS, time + Math.min(delta, .25f));
                if (time >= TOTAL_SECONDS) {
                    finish();
                    return;
                }
            }
        }

        int windowWidth = Gdx.graphics.getWidth();
        int windowHeight = Gdx.graphics.getHeight();
        float scale = Math.min(windowWidth / WIDTH, windowHeight / HEIGHT);
        int viewportWidth = Math.max(1, Math.round(WIDTH * scale));
        int viewportHeight = Math.max(1, Math.round(HEIGHT * scale));
        int viewportX = (windowWidth - viewportWidth) / 2;
        int viewportY = (windowHeight - viewportHeight) / 2;

        Gdx.gl.glViewport(viewportX, viewportY, viewportWidth, viewportHeight);
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        draw();
        batch.end();
    }

    private void draw() {
        fillRect(0, 0, WIDTH, HEIGHT, Color.BLACK);
        if (time < INTRO_SECONDS) {
            drawIntro(time);
        } else if (time < INTRO_SECONDS + 6f * CHARACTER_SECONDS) {
            float relative = time - INTRO_SECONDS;
            int index = Math.min(5, (int) (relative / CHARACTER_SECONDS));
            drawCharacter(index, relative - index * CHARACTER_SECONDS);
        } else if (time < INTRO_SECONDS + 6f * CHARACTER_SECONDS + END_CARD_1_SECONDS) {
            drawCard(final01, time - INTRO_SECONDS - 6f * CHARACTER_SECONDS,
                    END_CARD_1_SECONDS, true);
        } else if (time < INTRO_SECONDS + 6f * CHARACTER_SECONDS + END_CARD_1_SECONDS + END_CARD_2_SECONDS) {
            drawCard(final02, time - INTRO_SECONDS - 6f * CHARACTER_SECONDS - END_CARD_1_SECONDS,
                    END_CARD_2_SECONDS, false);
        }
    }

    private void drawIntro(float seconds) {
        float progress = clamp(seconds / 6.5f);
        drawCover(fundo, 1f + 4f * (1f - easeOut(progress)));
        if (seconds > 8.25f) {
            float fade = clamp((seconds - 8.25f) / 1.25f);
            fillRect(0, 0, WIDTH, HEIGHT, argb((int) (255 * fade), 2, 7, 18));
            drawPixelBlocks(fade, (int) (seconds * 30f));
        }
        if (seconds < .6f) {
            fillRect(0, 0, WIDTH, HEIGHT,
                    argb((int) (200 * (1f - seconds / .6f)), 0, 0, 0));
        }
    }

    private void drawCharacter(int index, float localSeconds) {
        drawGameBackground();
        panel(new Box(52, 112, 806, 838), argb(148, 7, 10, 28), argb(150, 50, 216, 211));
        panel(new Box(916, 152, 924, 798), argb(186, 6, 12, 29), argb(165, 243, 184, 68));
        fillTop(new Box(918, 248, 10, 582), argb(215, 243, 184, 68));
        drawLabel("DANIEL DO BOLO'S ADVENTURE", 78, 44, 28, rgb(255, 219, 107), Align.LEFT);
        drawLabel("PERSONAGEM " + String.format("%02d", index + 1) + " / 06", 1810, 47, 22,
                rgb(112, 231, 226), Align.RIGHT);
        drawLabel("PIXEL ART  .  DESENHO  .  HUMANO", 960, 890, 18,
                rgb(165, 211, 217), Align.CENTER);

        long frame = (long) (localSeconds * 30f);
        if (localSeconds < 6f) {
            drawFitted(art[index][0], new Box(70, 145, 720, 820), 1f, 0, 0);
        } else if (localSeconds < 14f) {
            drawMorph(art[index][0], art[index][1], clamp((localSeconds - 6f) / 8f), frame);
        } else if (localSeconds < 18f) {
            drawFitted(art[index][1], new Box(70, 145, 720, 820), 1f, 0, 0);
        } else if (localSeconds < 26f) {
            drawMorph(art[index][1], art[index][2], clamp((localSeconds - 18f) / 8f), frame);
        } else {
            drawFitted(art[index][2], new Box(70, 145, 720, 820), 1f, 0, 0);
        }

        float entrance = easeOut(clamp((localSeconds - .15f) / 1.45f));
        float offset = (1f - entrance) * -245f;
        drawLabel(NAMES[index], 980, 206 + offset, CHARACTER_TITLE_SIZE,
                rgb(255, 207, 77), Align.LEFT);
        fillTop(new Box(980, 295 + offset, 820, 4), rgb(62, 222, 212));
        drawFullCopy(index, offset);

        if (localSeconds < .85f) {
            fillRect(0, 0, WIDTH, HEIGHT,
                    argb((int) (230 * (1f - easeOut(localSeconds / .85f))), 0, 0, 0));
        } else if (localSeconds > 31.1f) {
            float fade = clamp((localSeconds - 31.1f) / .9f);
            fillRect(0, 0, WIDTH, HEIGHT, argb((int) (245 * fade), 0, 0, 0));
            drawPixelBlocks(fade, (int) frame);
        }
    }

    /** Mostra toda a descricao de uma vez; o tamanho reduz suavemente se ela for longa. */
    private void drawFullCopy(int index, float offset) {
        float textSize = CHARACTER_TEXT_SIZE;
        String[] lines = wrapCopy(COPY[index], textSize);
        while (textSize > 24f) {
            float lineSpacing = textSize * 1.45f;
            float maxLines = 1f + (TEXT_BOTTOM - 340f) / lineSpacing;
            if (lines.length <= maxLines) break;
            textSize -= 2f;
            lines = wrapCopy(COPY[index], textSize);
        }

        Color textColor = rgb(238, 246, 248);
        float y = 340 + offset;
        for (String line : lines) {
            if (!line.isEmpty()) {
                drawLabel(line, 980, y, textSize, textColor, Align.LEFT);
            }
            y += textSize * 1.45f;
        }
    }

    private String[] wrapCopy(String[] source, float textSize) {
        BitmapFont font = ctx.assets.getHighResFont();
        float oldScaleX = font.getData().scaleX;
        float oldScaleY = font.getData().scaleY;
        font.getData().setScale(textSize / 64f);
        List<String> result = new ArrayList<>();
        for (String paragraph : source) {
            if (paragraph.isEmpty()) {
                result.add("");
                continue;
            }
            String current = "";
            for (String word : paragraph.split(" ")) {
                String candidate = current.isEmpty() ? word : current + " " + word;
                layout.setText(font, candidate);
                if (layout.width <= TEXT_MAX_WIDTH || current.isEmpty()) {
                    current = candidate;
                } else {
                    result.add(current);
                    current = word;
                }
            }
            if (!current.isEmpty()) result.add(current);
        }
        font.getData().setScale(oldScaleX, oldScaleY);
        return result.toArray(new String[0]);
    }

    private void drawMorph(Texture first, Texture second, float progress, long frame) {
        Box box = new Box(70, 145, 720, 820);
        drawFitted(first, box, 1f + .018f * (float) Math.sin(Math.PI * progress), 0, 0);
        if (progress >= .97f) {
            drawFitted(second, box, 1f + .035f * (float) Math.sin(Math.PI * progress), 0, 0);
            return;
        }
        if (progress <= .03f) return;

        Rectangle destination = fittedRect(second, box, 1f + .035f * (float) Math.sin(Math.PI * progress), 0, 0);
        float cellW = box.w / 12f;
        float cellH = box.h / 14f;
        for (int row = 0; row < 14; row++) {
            for (int col = 0; col < 12; col++) {
                float hash = cellHash(col, row, frame);
                float moving = .06f * (float) Math.sin(frame * .11f + col * .7f);
                if (hash + moving < progress) {
                    float cellX = box.x + col * cellW;
                    float cellTop = topY(box.y + row * cellH);
                    float cellBottom = topY(box.y + (row + 1) * cellH);
                    drawTexturePart(second, destination, cellX, cellBottom, cellW, cellTop - cellBottom);
                }
            }
        }
    }

    private void drawCard(Texture source, float localSeconds, float duration, boolean cartridge) {
        drawCover(source, 1.08f);
        fillRect(0, 0, WIDTH, HEIGHT, argb(56, 4, 8, 22));
        drawFitted(source, new Box(85, 60, 1750, 960),
                1f + .025f * easeOut(clamp(localSeconds)), 0, 0);
        if (cartridge) {
            drawLabel("SE VOCE E FAN DE RETRO GAME,", 960, 120, 54, rgb(255, 226, 114), Align.CENTER);
            drawLabel("ADQUIRA A VERSAO EM CARTUCHO", 960, 192, 56, rgb(255, 226, 114), Align.CENTER);
            drawLabel("PARA SUPER NINTENDO!", 960, 264, 56, rgb(255, 226, 114), Align.CENTER);
        } else {
            drawLabel("DANIEL DO BOLO'S ADVENTURE", 960, 130, 70, rgb(255, 225, 106), Align.CENTER);
            drawLabel("DISPONIVEL NA GOOGLE PLAY", 960, 220, 56, Color.WHITE, Align.CENTER);
        }
        drawLabel("fabioad.com.br/danieldobolo", 960, 900, 58, rgb(111, 247, 227), Align.CENTER);

        float alpha = 1f;
        if (localSeconds < .9f) alpha *= easeOut(clamp(localSeconds / .9f));
        if (!cartridge && localSeconds > duration - 3f) alpha *= clamp((duration - localSeconds) / 3f);
        if (alpha < .999f) fillRect(0, 0, WIDTH, HEIGHT, argb((int) (255f * (1f - alpha)), 0, 0, 0));
    }

    private void drawGameBackground() {
        for (int i = 0; i < 12; i++) {
            float t = i / 11f;
            fillRect(0, i * HEIGHT / 12f, WIDTH, HEIGHT / 12f,
                    new Color(.055f + .02f * t, .06f + .12f * t, .15f + .14f * t, 1f));
        }
        for (int y = 0; y < 1080; y += 54) fillTop(new Box(0, y, 1920, 2), argb(26, 90, 178, 207));
        for (int x = 0; x < 1920; x += 96) fillTop(new Box(x, 0, 2, 1080), argb(18, 72, 123, 182));
        fillTop(new Box(0, 0, 1920, 12), rgb(243, 186, 66));
        fillTop(new Box(0, 1068, 1920, 12), rgb(14, 199, 190));
    }

    private void panel(Box box, Color fill, Color stroke) {
        fillTop(box, fill);
        float border = 3f;
        fillTop(new Box(box.x, box.y, box.w, border), stroke);
        fillTop(new Box(box.x, box.y + box.h - border, box.w, border), stroke);
        fillTop(new Box(box.x, box.y, border, box.h), stroke);
        fillTop(new Box(box.x + box.w - border, box.y, border, box.h), stroke);
    }

    private void drawFitted(Texture texture, Box box, float scale, float dx, float dy) {
        if (texture == null) return;
        Rectangle r = fittedRect(texture, box, scale, dx, dy);
        batch.setColor(Color.WHITE);
        batch.draw(texture, r.x, r.y, r.width, r.height);
    }

    private Rectangle fittedRect(Texture texture, Box box, float scale, float dx, float dy) {
        float factor = Math.min(box.w / texture.getWidth(), box.h / texture.getHeight()) * scale;
        float width = texture.getWidth() * factor;
        float height = texture.getHeight() * factor;
        float centerX = box.x + box.w / 2f + dx;
        float centerY = topY(box.y) - box.h / 2f - dy;
        return new Rectangle(centerX - width / 2f, centerY - height / 2f, width, height);
    }

    private void drawCover(Texture texture, float zoom) {
        if (texture == null) return;
        float factor = Math.max(WIDTH / texture.getWidth(), HEIGHT / texture.getHeight()) * zoom;
        float width = texture.getWidth() * factor;
        float height = texture.getHeight() * factor;
        batch.setColor(Color.WHITE);
        batch.draw(texture, (WIDTH - width) / 2f, (HEIGHT - height) / 2f, width, height);
    }

    private void drawTexturePart(Texture texture, Rectangle destination, float x, float y,
                                 float width, float height) {
        float left = Math.max(x, destination.x);
        float right = Math.min(x + width, destination.x + destination.width);
        float bottom = Math.max(y, destination.y);
        float top = Math.min(y + height, destination.y + destination.height);
        if (right <= left || top <= bottom) return;
        int srcX = Math.max(0, Math.round((left - destination.x) / destination.width * texture.getWidth()));
        int srcY = Math.max(0, Math.round((bottom - destination.y) / destination.height * texture.getHeight()));
        int srcW = Math.max(1, Math.min(texture.getWidth() - srcX,
                Math.round((right - left) / destination.width * texture.getWidth())));
        int srcH = Math.max(1, Math.min(texture.getHeight() - srcY,
                Math.round((top - bottom) / destination.height * texture.getHeight())));
        batch.setColor(Color.WHITE);
        batch.draw(texture, left, bottom, right - left, top - bottom,
                srcX, srcY, srcW, srcH, false, false);
    }

    private void drawPixelBlocks(float amount, int frame) {
        int count = (int) (42 * clamp(amount));
        for (int i = 0; i < count; i++) {
            int seed = i * 1103515245 + frame * 12345;
            int x = Math.abs(seed) % 120 * 16;
            int y = Math.abs(seed / 97) % 68 * 16;
            int size = 16 + (Math.abs(seed / 31) % 3) * 16;
            fillTop(new Box(x, y, size, size), argb((int) (170 * amount),
                    (i % 3 == 0) ? 244 : 58, (i % 3 == 0) ? 190 : 224,
                    (i % 3 == 0) ? 63 : 214));
        }
    }

    private void drawLabel(String value, float x, float y, float sourceSize, Color color, Align align) {
        BitmapFont font = ctx.assets.getHighResFont();
        float oldScaleX = font.getData().scaleX;
        float oldScaleY = font.getData().scaleY;
        Color oldColor = font.getColor().cpy();
        font.getData().setScale(Math.max(.25f, sourceSize / 64f));
        layout.setText(font, value);
        float drawX = x;
        if (align == Align.CENTER) drawX -= layout.width / 2f;
        if (align == Align.RIGHT) drawX -= layout.width;
        float drawY = topY(y);
        font.setColor(Color.BLACK);
        font.draw(batch, value, drawX + 6f, drawY - 7f);
        font.setColor(color);
        font.draw(batch, value, drawX, drawY);
        font.getData().setScale(oldScaleX, oldScaleY);
        font.setColor(oldColor);
    }

    private void fillTop(Box box, Color color) {
        fillRect(box.x, HEIGHT - box.y - box.h, box.w, box.h, color);
    }

    private void fillRect(float x, float y, float width, float height, Color color) {
        batch.setColor(color);
        batch.draw(whitePixel, x, y, width, height);
        batch.setColor(Color.WHITE);
    }

    private float topY(float sourceY) {
        return HEIGHT - sourceY;
    }

    private boolean anyInputPressed() {
        if (Gdx.input.justTouched()) return true;
        for (int key = 0; key < 256; key++) {
            if (Gdx.input.isKeyJustPressed(key)) return true;
        }
        for (GameInput.Action action : GameInput.Action.values()) {
            if (ctx.input.isPressed(action)) return true;
        }
        return false;
    }

    private void finish() {
        finished = true;
        game.changeScreen(new TitleScreen(game));
    }

    private Texture load(String path) {
        if (!Gdx.files.internal(path).exists()) {
            Gdx.app.error("TrailerScreen", "Asset ausente: " + path);
            return null;
        }
        Texture texture = new Texture(Gdx.files.internal(path));
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return texture;
    }

    @Override
    public void dispose() {
        batch.dispose();
        whitePixel.dispose();
        dispose(fundo);
        dispose(final01);
        dispose(final02);
        for (Texture[] row : art) {
            for (Texture texture : row) dispose(texture);
        }
    }

    private static Texture createWhitePixel() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private static void dispose(Texture texture) {
        if (texture != null) texture.dispose();
    }

    private static float clamp(float value) {
        return Math.max(0f, Math.min(1f, value));
    }

    private static float easeOut(float value) {
        value = clamp(value);
        return 1f - (float) Math.pow(1f - value, 3);
    }

    private static float cellHash(int col, int row, long frame) {
        long value = col * 92837111L + row * 689287499L + frame * 31L;
        value = (value ^ (value >>> 16)) * 0x45d9f3bL;
        value = (value ^ (value >>> 16)) * 0x45d9f3bL;
        value = value ^ (value >>> 16);
        return (value & 0x7fffffffL) / (float) 0x7fffffffL;
    }

    private static Color rgb(int red, int green, int blue) {
        return new Color(red / 255f, green / 255f, blue / 255f, 1f);
    }

    private static Color argb(int alpha, int red, int green, int blue) {
        return new Color(red / 255f, green / 255f, blue / 255f, clamp(alpha / 255f));
    }

    private enum Align { LEFT, CENTER, RIGHT }

    private static final class Box {
        final float x;
        final float y;
        final float w;
        final float h;

        Box(float x, float y, float w, float h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }
}
