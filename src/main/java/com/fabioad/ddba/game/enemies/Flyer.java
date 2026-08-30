package com.fabioad.ddba.game.enemies;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.fabioad.ddba.game.maps.TileMap;

/**
 * Flyer - inimigo "voador".
 * =============================================================================
 * IA: ignora a gravidade e voa horizontalmente descrevendo uma ONDA SENOIDAL na
 * vertical. Vira ao encontrar parede na sua altura. Sobrevoa buracos (nao cai).
 *
 * ALGORITMO:
 *   y = baseY + sin(t * freq) * amplitude
 *   x avanca a velocidade constante na direcao atual.
 *   Isso cria um voo ondulante barato e classico dos jogos 16 bits.
 *
 * PORTABILIDADE:
 *   - SNES: o seno viria de uma TABELA de senos pre-computada (LUT) na ROM, pois
 *     o 65816 nao tem trigonometria em hardware.
 */
public final class Flyer extends Enemy {
    private static final float SPEED = 40f;
    private static final float AMPLITUDE = 20f;
    private static final float FREQ = 3f;

    private final float baseY;

    public Flyer(Animation<TextureRegion> anim, TileMap map, float x, float y) {
        super(anim, map, x, y, 16, 16);
        this.baseY = y;
        this.health = 1;
        this.points = 250;
    }

    @Override
    protected void updateAI(float dt) {
        float speed = facingRight ? SPEED : -SPEED;
        box.x += speed * dt;

        // Voo ondulante (LUT de seno em um porte real).
        box.y = baseY + (float) Math.sin(stateTime * FREQ) * AMPLITUDE;

        // Vira ao encontrar parede solida a frente (na sua altura).
        int ts = map.getTileSize();
        float aheadX = facingRight ? box.right() + 1 : box.x - 1;
        int col = (int) Math.floor(aheadX / ts);
        int row = (int) Math.floor(box.centerY() / ts);
        if (map.isSolid(col, row)) {
            facingRight = !facingRight;
        }
        // Tambem vira nos limites do mundo.
        if (box.x < 0) { box.x = 0; facingRight = true; }
        if (box.right() > map.getWidthPixels()) { box.x = map.getWidthPixels() - box.width; facingRight = false; }
    }
}
