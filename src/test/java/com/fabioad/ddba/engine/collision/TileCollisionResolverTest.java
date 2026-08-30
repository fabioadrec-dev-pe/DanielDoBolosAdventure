package com.fabioad.ddba.engine.collision;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes do resolvedor de colisao por tiles (logica pura, sem OpenGL).
 * Verifica os casos classicos: pousar no chao, bater no teto e nas paredes.
 */
class TileCollisionResolverTest {

    /** Mundo de teste 5x5 tiles de 16px, com uma linha de chao solido na base. */
    private static TileSoliditySource groundWorld() {
        return new TileSoliditySource() {
            @Override public int getColumns() { return 5; }
            @Override public int getRows() { return 5; }
            @Override public int getTileSize() { return 16; }
            @Override public boolean isSolid(int col, int row) {
                return row == 0; // linha de baixo e chao
            }
        };
    }

    @Test
    void landsOnGround() {
        TileCollisionResolver r = new TileCollisionResolver();
        // Caixa 16x16 caindo em direcao ao chao (row 0 ocupa y 0..16).
        AABB box = new AABB(16, 20, 16, 16);
        TileCollisionResolver.CollisionResult res = r.move(box, 0, -10, groundWorld());
        assertTrue(res.onGround, "deveria detectar chao");
        assertEquals(16f, box.y, 0.001f, "deveria encostar exatamente no topo do chao");
    }

    @Test
    void hitsWallOnRight() {
        TileCollisionResolver r = new TileCollisionResolver();
        TileSoliditySource wall = new TileSoliditySource() {
            @Override public int getColumns() { return 5; }
            @Override public int getRows() { return 5; }
            @Override public int getTileSize() { return 16; }
            @Override public boolean isSolid(int col, int row) { return col == 3; }
        };
        AABB box = new AABB(16, 32, 16, 16); // coluna 1..2, movendo para a direita
        TileCollisionResolver.CollisionResult res = r.move(box, 20, 0, wall);
        assertTrue(res.hitRight, "deveria bater na parede a direita");
        assertEquals(48f - 16f, box.x, 0.001f, "deveria encostar na coluna 3 (x=48)");
    }

    @Test
    void freeFallWhenNoGround() {
        TileCollisionResolver r = new TileCollisionResolver();
        TileSoliditySource empty = new TileSoliditySource() {
            @Override public int getColumns() { return 5; }
            @Override public int getRows() { return 5; }
            @Override public int getTileSize() { return 16; }
            @Override public boolean isSolid(int col, int row) { return false; }
        };
        AABB box = new AABB(16, 40, 16, 16);
        TileCollisionResolver.CollisionResult res = r.move(box, 0, -10, empty);
        assertFalse(res.onGround);
        assertEquals(30f, box.y, 0.001f);
    }
}
