package com.fabioad.ddba.game.enemies;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.fabioad.ddba.engine.assets.Assets;
import com.fabioad.ddba.game.maps.TileMap;

/**
 * EnemyFactory
 * =============================================================================
 * OBJETIVO DA CLASSE:
 *   Traduzir um "tipo" textual (vindo dos dados da fase) na instancia concreta do
 *   inimigo correto, injetando a animacao adequada e o mapa. Centraliza a criacao
 *   (padrao Factory), evitando 'switch' espalhados pela PlayScreen.
 *
 * PORTABILIDADE:
 *   - SNES: corresponde a rotina que, ao ler a lista de objetos da fase, aloca o
 *     slot de inimigo e inicializa seus campos conforme o 'type'.
 */
public final class EnemyFactory {

    private EnemyFactory() {
    }

    public static Enemy create(String type, Assets assets, TileMap map, float x, float y) {
        Animation<TextureRegion> a = assets.enemyAnims.get(type);
        switch (type) {
            case "flyer": return new Flyer(a, map, x, y);
            case "fast": return new FastRunner(a, map, x, y);
            case "tank": return new Tank(a, map, x, y);
            case "boss": return new Boss(a, map, x, y);
            case "walker":
            default: return new Walker(a, map, x, y);
        }
    }
}
