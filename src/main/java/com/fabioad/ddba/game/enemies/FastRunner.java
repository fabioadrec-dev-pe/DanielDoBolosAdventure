package com.fabioad.ddba.game.enemies;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.fabioad.ddba.game.maps.TileMap;

/**
 * FastRunner - inimigo "rapido".
 * =============================================================================
 * IA: mesma patrulha do Walker, porem MUITO mais veloz, exigindo timing preciso
 * do jogador. Morre com 1 pisao.
 *
 * PORTABILIDADE: mesma rotina do walker com uma constante de velocidade maior;
 * no SNES bastaria outra entrada na tabela de velocidade por tipo.
 */
public final class FastRunner extends Enemy {
    private static final float SPEED = 95f;

    public FastRunner(Animation<TextureRegion> anim, TileMap map, float x, float y) {
        super(anim, map, x, y, 16, 16);
        this.health = 1;
        this.points = 300;
    }

    @Override
    protected void updateAI(float dt) {
        patrol(dt, SPEED);
    }
}
