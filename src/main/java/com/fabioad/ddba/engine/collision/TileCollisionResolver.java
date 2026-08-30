package com.fabioad.ddba.engine.collision;

/**
 * TileCollisionResolver
 * =============================================================================
 * OBJETIVO DA CLASSE:
 *   Mover uma caixa (AABB) por um deslocamento (dx, dy) resolvendo colisoes
 *   contra tiles solidos de um mapa em grade. Este e o coracao da "colisao por
 *   tiles" de qualquer plataforma 16 bits.
 *
 * ALGORITMO (colisao por eixo separado - "swept AABB simplificado"):
 *   Resolvemos UM eixo por vez (primeiro X, depois Y). Para cada eixo:
 *     1) Aplica o deslocamento naquele eixo.
 *     2) Calcula o intervalo de tiles que a caixa agora cobre.
 *     3) Se algum tile e solido, empurra a caixa exatamente para fora dele
 *        (encosta na borda) e zera a velocidade daquele eixo.
 *   Resolver eixo a eixo evita "travar" em quinas e e o metodo classico usado
 *   em Mario/Sonic-like. E robusto para velocidades moderadas (limitamos a
 *   velocidade de queda em GameConfig para evitar "tunneling").
 *
 * DECISOES DE ARQUITETURA:
 *   - O resolvedor NAO conhece o jogo: recebe um TileSoliditySource (contrato).
 *   - Retorna um CollisionResult com flags (chao/teto/parede) para a logica de
 *     gameplay (ex.: "onGround" habilita pular).
 *
 * PORTABILIDADE:
 *   - SNES/68000: identico em ponto-fixo. A varredura de tiles e apenas divisao
 *     por 16 (deslocamento >> 4) e leitura do tilemap na VRAM/RAM.
 */
public final class TileCollisionResolver {

    /** Resultado da resolucao: quais lados colidiram neste passo. */
    public static final class CollisionResult {
        public boolean onGround;   // colidiu por baixo (pisando em algo)
        public boolean hitCeiling; // colidiu por cima
        public boolean hitLeft;    // colidiu parede a esquerda
        public boolean hitRight;   // colidiu parede a direita

        public void reset() {
            onGround = hitCeiling = hitLeft = hitRight = false;
        }
    }

    private final CollisionResult result = new CollisionResult();

    /**
     * Move a caixa por (dx, dy), resolvendo colisoes.
     *
     * @param box   caixa a mover (modificada in-place)
     * @param dx    deslocamento horizontal deste passo (px)
     * @param dy    deslocamento vertical deste passo (px)
     * @param world fonte de solidez dos tiles
     * @return flags de colisao deste passo
     */
    public CollisionResult move(AABB box, float dx, float dy, TileSoliditySource world) {
        result.reset();

        // ---- EIXO X ----
        box.x += dx;
        if (dx > 0) {
            resolveAxisX(box, world, true);
        } else if (dx < 0) {
            resolveAxisX(box, world, false);
        }

        // ---- EIXO Y ----
        box.y += dy;
        if (dy > 0) {
            resolveAxisY(box, world, true);
        } else if (dy < 0) {
            resolveAxisY(box, world, false);
        }

        return result;
    }

    /** Resolve colisao no eixo X. movingRight define de qual lado empurrar. */
    private void resolveAxisX(AABB box, TileSoliditySource world, boolean movingRight) {
        int ts = world.getTileSize();
        // Linhas que a caixa cobre verticalmente.
        int rowTop = clamp((int) Math.floor((box.top() - 0.001f) / ts), 0, world.getRows() - 1);
        int rowBottom = clamp((int) Math.floor(box.y / ts), 0, world.getRows() - 1);

        if (movingRight) {
            int col = (int) Math.floor((box.right() - 0.001f) / ts);
            if (isSolidColumn(world, col, rowBottom, rowTop)) {
                box.x = col * ts - box.width; // encosta a direita no tile
                result.hitRight = true;
            }
        } else {
            int col = (int) Math.floor(box.x / ts);
            if (isSolidColumn(world, col, rowBottom, rowTop)) {
                box.x = (col + 1) * ts; // encosta a esquerda no tile
                result.hitLeft = true;
            }
        }
    }

    /** Resolve colisao no eixo Y. movingUp define de qual lado empurrar. */
    private void resolveAxisY(AABB box, TileSoliditySource world, boolean movingUp) {
        int ts = world.getTileSize();
        int colLeft = clamp((int) Math.floor(box.x / ts), 0, world.getColumns() - 1);
        int colRight = clamp((int) Math.floor((box.right() - 0.001f) / ts), 0, world.getColumns() - 1);

        if (movingUp) {
            int row = (int) Math.floor((box.top() - 0.001f) / ts);
            if (isSolidRow(world, row, colLeft, colRight)) {
                box.y = row * ts - box.height; // bate a cabeca no teto
                result.hitCeiling = true;
            }
        } else {
            int row = (int) Math.floor(box.y / ts);
            if (isSolidRow(world, row, colLeft, colRight)) {
                box.y = (row + 1) * ts; // pousa no chao
                result.onGround = true;
            }
        }
    }

    private boolean isSolidColumn(TileSoliditySource world, int col, int rowFrom, int rowTo) {
        if (col < 0 || col >= world.getColumns()) return false;
        int lo = Math.min(rowFrom, rowTo);
        int hi = Math.max(rowFrom, rowTo);
        for (int r = lo; r <= hi; r++) {
            if (r >= 0 && r < world.getRows() && world.isSolid(col, r)) return true;
        }
        return false;
    }

    private boolean isSolidRow(TileSoliditySource world, int row, int colFrom, int colTo) {
        if (row < 0 || row >= world.getRows()) return false;
        int lo = Math.min(colFrom, colTo);
        int hi = Math.max(colFrom, colTo);
        for (int c = lo; c <= hi; c++) {
            if (c >= 0 && c < world.getColumns() && world.isSolid(c, row)) return true;
        }
        return false;
    }

    private static int clamp(int v, int lo, int hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }
}
