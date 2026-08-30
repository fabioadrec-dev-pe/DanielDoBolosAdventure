package com.fabioad.ddba.game;

import com.fabioad.ddba.engine.collision.AABB;
import com.fabioad.ddba.engine.core.GameConfig;
import com.fabioad.ddba.game.core.GameSession;
import com.fabioad.ddba.game.maps.StageData;
import com.fabioad.ddba.game.maps.StageFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes de logica de jogo puros (sem OpenGL): sessao, colisao AABB e geracao
 * procedural das fases. Garante que as 5 fases sao construiveis e jogaveis.
 */
class GameLogicTest {

    @Test
    void aabbOverlap() {
        AABB a = new AABB(0, 0, 16, 16);
        AABB b = new AABB(8, 8, 16, 16);
        AABB c = new AABB(32, 32, 16, 16);
        assertTrue(a.overlaps(b));
        assertTrue(!a.overlaps(c));
    }

    @Test
    void coinGivesExtraLifeAtHundred() {
        GameSession s = new GameSession();
        int startLives = s.getLives();
        for (int i = 0; i < 100; i++) s.addCoin();
        assertEquals(startLives + 1, s.getLives(), "100 moedas devem dar 1 vida extra");
        assertEquals(0, s.getCoins(), "contador de moedas deve zerar apos vida extra");
        assertTrue(s.getScore() > 0);
    }

    @Test
    void loseLifeEndsGameAtZero() {
        GameSession s = new GameSession();
        boolean alive = true;
        for (int i = 0; i < GameConfig.START_LIVES; i++) {
            alive = s.loseLife();
        }
        assertTrue(!alive, "apos perder todas as vidas, jogo acaba");
    }

    @Test
    void allFiveStagesBuild() {
        for (int i = 0; i < GameConfig.TOTAL_STAGES; i++) {
            StageData data = StageFactory.build(i);
            assertNotNull(data.map, "fase " + i + " deve ter mapa");
            assertTrue(data.goalX > data.playerStartX, "objetivo deve ficar a frente do inicio");
            assertTrue(data.map.getWidthPixels() > GameConfig.VIRTUAL_WIDTH, "fase deve rolar");
            assertNotNull(data.name);
        }
    }

    @Test
    void lastStageIsBoss() {
        StageData boss = StageFactory.build(GameConfig.TOTAL_STAGES - 1);
        assertTrue(boss.bossStage, "ultima fase deve ser de chefe");
        boolean hasBoss = false;
        for (StageData.EnemySpawn e : boss.enemies) {
            if (e.type.equals("boss")) hasBoss = true;
        }
        assertTrue(hasBoss, "fase de chefe deve conter um boss");
    }
}
