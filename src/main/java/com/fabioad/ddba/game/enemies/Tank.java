package com.fabioad.ddba.game.enemies;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.fabioad.ddba.game.maps.TileMap;

/**
 * Tank - inimigo "resistente".
 * =============================================================================
 * IA: patrulha bem lenta, porem AGUENTA 3 pisoes antes de ser derrotado. Grande
 * (24x24) e pesado; obriga o jogador a insistir ou desviar.
 *
 * PORTABILIDADE: identico ao walker, mudando apenas 'health' (contador de vida)
 * e o tamanho. No SNES, um sprite de 32x32 (OBJ grande) com HP > 1.
 */
public final class Tank extends Enemy {
    private static final float SPEED = 18f;

    public Tank(Animation<TextureRegion> anim, TileMap map, float x, float y) {
        super(anim, map, x, y, 24, 24);
        this.health = 3;
        this.points = 500;
    }

    @Override
    protected void updateAI(float dt) {
        patrol(dt, SPEED);
    }
}
