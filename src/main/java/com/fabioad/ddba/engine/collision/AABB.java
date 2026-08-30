package com.fabioad.ddba.engine.collision;

/**
 * AABB (Axis-Aligned Bounding Box)
 * =============================================================================
 * OBJETIVO DA CLASSE:
 *   Representar uma caixa de colisao alinhada aos eixos (sem rotacao) em pixels.
 *   E a primitiva de colisao mais barata e mais usada em jogos 2D 16 bits.
 *
 * CONVENCAO:
 *   (x, y) e o canto INFERIOR-ESQUERDO (coordenadas Y-up, como o mundo virtual).
 *   width/height sao as dimensoes.
 *
 * ALGORITMO (overlap):
 *   Duas AABBs colidem se, e somente se, se sobrepoem NOS DOIS eixos
 *   simultaneamente (teorema dos eixos separadores para caixas alinhadas).
 *
 * PORTABILIDADE:
 *   - SNES/68000: as coordenadas virariam inteiros (ou ponto-fixo 8.8); a logica
 *     de sobreposicao e identica e extremamente barata (somas e comparacoes).
 */
public final class AABB {

    public float x;
    public float y;
    public float width;
    public float height;

    public AABB() {
    }

    public AABB(float x, float y, float width, float height) {
        set(x, y, width, height);
    }

    public void set(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public float right() {
        return x + width;
    }

    public float top() {
        return y + height;
    }

    public float centerX() {
        return x + width / 2f;
    }

    public float centerY() {
        return y + height / 2f;
    }

    /** True se esta caixa se sobrepoe a outra (colisao em ambos eixos). */
    public boolean overlaps(AABB o) {
        return x < o.right() && right() > o.x && y < o.top() && top() > o.y;
    }

    public boolean contains(float px, float py) {
        return px >= x && px <= right() && py >= y && py <= top();
    }
}
