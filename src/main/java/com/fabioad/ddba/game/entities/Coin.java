package com.fabioad.ddba.game.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Coin - moeda colecionavel.
 * =============================================================================
 * OBJETIVO: item estatico com animacao de giro. Ao ser coletado, some (alive=false)
 * e concede pontos/moeda a sessao. Colisao simples por sobreposicao de AABB.
 *
 * PORTABILIDADE: no SNES, moedas costumam ser tiles animados do BG (troca de tile
 * por VBlank) ou OBJs pequenos; a coleta apaga o tile/OBJ e incrementa o contador.
 */
public final class Coin extends Entity {

    private final Animation<TextureRegion> anim;

    public Coin(Animation<TextureRegion> anim, float x, float y) {
        super(x, y, 16, 16);
        this.anim = anim;
    }

    @Override
    public void update(float dt) {
        stateTime += dt;
    }

    @Override
    public void draw(SpriteBatch batch) {
        if (!alive || anim == null) return;
        batch.draw(anim.getKeyFrame(stateTime, true), box.x, box.y, box.width, box.height);
    }
}
