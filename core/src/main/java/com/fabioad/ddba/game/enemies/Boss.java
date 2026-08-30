package com.fabioad.ddba.game.enemies;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.fabioad.ddba.engine.core.GameConfig;
import com.fabioad.ddba.game.maps.TileMap;

/**
 * Boss - o "chefe".
 * =============================================================================
 * IA: patrulha em velocidade media e da SALTOS periodicos (padrao previsivel,
 * porem ameacador). Precisa de 5 pisoes para ser derrotado. Vale muitos pontos.
 *
 * DECISAO DE DESIGN:
 *   Padrao simples e "lisivel" pelo jogador (andar + pular a cada X segundos),
 *   fiel aos chefes 16 bits: o desafio esta em ler o padrao, nao em aleatoriedade.
 *
 * PORTABILIDADE:
 *   - SNES: chefes costumam ser uma MAQUINA DE ESTADOS (andar/pular/atacar) com
 *     temporizadores; este exemplo mostra a forma mais enxuta dessa maquina.
 */
public final class Boss extends Enemy {
    private static final float SPEED = 45f;
    private static final float JUMP_INTERVAL = 2.2f;

    private float jumpTimer;

    public Boss(Animation<TextureRegion> anim, TileMap map, float x, float y) {
        super(anim, map, x, y, 48, 48);
        this.health = 5;
        this.points = 5000;
    }

    @Override
    protected void updateAI(float dt) {
        jumpTimer += dt;
        // Salto periodico quando esta no chao.
        if (onGround && jumpTimer >= JUMP_INTERVAL) {
            vy = GameConfig.PLAYER_JUMP_VELOCITY * 0.9f;
            onGround = false;
            jumpTimer = 0;
        }
        patrol(dt, SPEED);
    }
}
