package com.fabioad.ddba.engine.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * TextUtil
 * =============================================================================
 * OBJETIVO DA CLASSE:
 *   Utilitario de desenho de TEXTO com escala e alinhamento (centralizado),
 *   reutilizado por menus, titulo, apresentacao e creditos. Evita repetir a
 *   logica de medir/centralizar em cada tela (DRY).
 *
 * NITIDEZ (canvas 256x224):
 *   A fonte pixel e desenhada so em escalas inteiras (1x/2x) e em coordenadas
 *   inteiras — escalas fracionarias de Arial borravam no FBO com Nearest.
 *
 * PORTABILIDADE:
 *   - SNES: texto e composto por tiles de fonte; "centralizar" e calcular a
 *     coluna inicial pela contagem de caracteres * largura do tile.
 */
public final class TextUtil {

    private static final GlyphLayout LAYOUT = new GlyphLayout();

    private TextUtil() {
    }

    /**
     * Converte o "tamanho semantico" antigo em escala pixel inteira.
     * Fonte base e pequena (~8px): corpo = 1x, titulo = 2x.
     * A HUD do jogo usa 2x direto (quase o tamanho anterior).
     */
    public static float crispScale(float scale) {
        return scale >= 0.85f ? 2f : 1f;
    }

    /** Desenha texto centralizado horizontalmente em centerX, com base em y (topo). */
    public static void drawCentered(SpriteBatch batch, BitmapFont font, String text,
                                    float centerX, float y, float scale, Color color) {
        float px = font.getData().scaleX;
        float py = font.getData().scaleY;
        Color pc = font.getColor().cpy();

        float s = crispScale(scale);
        font.getData().setScale(s);
        font.setColor(color);
        LAYOUT.setText(font, text);
        float x = Math.round(centerX - LAYOUT.width / 2f);
        float yi = Math.round(y);
        font.draw(batch, text, x, yi);

        font.getData().setScale(px, py);
        font.setColor(pc);
    }

    /** Desenha texto alinhado a esquerda em (x, y) com escala/cor. */
    public static void draw(SpriteBatch batch, BitmapFont font, String text,
                            float x, float y, float scale, Color color) {
        float px = font.getData().scaleX;
        float py = font.getData().scaleY;
        Color pc = font.getColor().cpy();

        float s = crispScale(scale);
        font.getData().setScale(s);
        font.setColor(color);
        font.draw(batch, text, Math.round(x), Math.round(y));

        font.getData().setScale(px, py);
        font.setColor(pc);
    }

    public static float width(BitmapFont font, String text, float scale) {
        float px = font.getData().scaleX;
        float py = font.getData().scaleY;
        font.getData().setScale(crispScale(scale));
        LAYOUT.setText(font, text);
        float w = LAYOUT.width;
        font.getData().setScale(px, py);
        return w;
    }

    /** Quebra um paragrafo em linhas que caibam em maxWidth (na escala dada). */
    public static String[] wrap(BitmapFont font, String text, float scale, float maxWidth) {
        java.util.ArrayList<String> lines = new java.util.ArrayList<>();
        String[] paragraphs = text.split("\n", -1);
        for (String paragraph : paragraphs) {
            if (paragraph.isEmpty()) {
                lines.add("");
                continue;
            }
            String[] words = paragraph.split(" ");
            StringBuilder line = new StringBuilder();
            for (String word : words) {
                String trial = line.length() == 0 ? word : line + " " + word;
                if (width(font, trial, scale) <= maxWidth) {
                    if (line.length() > 0) line.append(' ');
                    line.append(word);
                } else {
                    if (line.length() > 0) lines.add(line.toString());
                    line.setLength(0);
                    line.append(word);
                }
            }
            if (line.length() > 0) lines.add(line.toString());
        }
        return lines.toArray(new String[0]);
    }
}
