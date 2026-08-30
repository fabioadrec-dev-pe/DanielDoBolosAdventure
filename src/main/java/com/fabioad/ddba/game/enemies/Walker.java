package com.fabioad.ddba.game.enemies;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.fabioad.ddba.game.maps.TileMap;

/**
 * Walker - inimigo "pequeno" e basico.
 * =============================================================================
 * IA: patrulha lenta de ida-e-volta, virando em paredes e beiras. Morre com 1
 * pisao. E o inimigo introdutorio (fase 1), ensinando a mecanica de "pular em cima".
 *
 * PORTABILIDADE: o equivalente do "Goomba"; no SNES seria a IA mais barata (so
 * anda e vira), ideal para muitos na tela ao mesmo tempo.
 */
public final class Walker extends Enemy {
    private static final float SPEED = 30f;

    public Walker(Animation<TextureRegion> anim, TileMap map, float x, float y) {
        super(anim, map, x, y, 24, 24);
        this.health = 1;
        this.points = 200;
    }

    @Override
    protected void updateAI(float dt) {
        patrol(dt, SPEED);
    }
}
