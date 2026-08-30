package com.fabioad.ddba.game.maps;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.fabioad.ddba.engine.collision.TileSoliditySource;
import com.fabioad.ddba.engine.core.GameConfig;
import com.fabioad.ddba.game.tiles.TileType;

/**
 * TileMap
 * =============================================================================
 * OBJETIVO DA CLASSE:
 *   Representar a GRADE de tiles de uma fase e:
 *     - fornecer a solidez ao resolvedor de colisao (implementa TileSoliditySource);
 *     - desenhar apenas os tiles VISIVEIS (culling por camera) para performance.
 *
 * CONVENCAO DE COORDENADAS:
 *   grid[row][col], onde row=0 e a BASE (chao) e cresce para CIMA (Y-up), casando
 *   com o mundo virtual e com o resolvedor de colisao (row = floor(y / tile)).
 *
 * ALGORITMO DE RENDER (culling):
 *   Em vez de desenhar todos os tiles, calculamos a janela de colunas/linhas
 *   visiveis a partir da posicao da camera e desenhamos so esse retangulo.
 *   Reduz drasticamente o numero de draws em fases grandes.
 *
 * PORTABILIDADE:
 *   - SNES: o PPU ja faz "culling" automatico desenhando so a janela 256x224 do
 *     tilemap conforme os registradores de scroll. Aqui reproduzimos em software.
 */
public final class TileMap implements TileSoliditySource {

    private final int[][] grid; // [row][col], row 0 = base
    private final int rows;
    private final int cols;
    private final int tileSize;

    public TileMap(int[][] grid) {
        this.grid = grid;
        this.rows = grid.length;
        this.cols = grid[0].length;
        this.tileSize = GameConfig.TILE_SIZE;
    }

    public int get(int col, int row) {
        if (col < 0 || col >= cols || row < 0 || row >= rows) return TileType.AIR;
        return grid[row][col];
    }

    public void set(int col, int row, int id) {
        if (col >= 0 && col < cols && row >= 0 && row < rows) {
            grid[row][col] = id;
        }
    }

    public boolean isHazard(int col, int row) {
        return TileType.isHazard(get(col, row));
    }

    public float getWidthPixels() {
        return cols * tileSize;
    }

    public float getHeightPixels() {
        return rows * tileSize;
    }

    /**
     * Desenha os tiles visiveis dentro da janela da camera.
     *
     * @param batch      lote de desenho ja ativo (entre begin/end)
     * @param regions    regioes do tileset indexadas pelo ID do tile
     * @param camLeft    borda esquerda da camera (px no mundo)
     * @param camBottom  borda inferior da camera (px no mundo)
     */
    public void render(SpriteBatch batch, TextureRegion[] regions, float camLeft, float camBottom) {
        if (regions == null || regions.length == 0) return;

        int firstCol = Math.max(0, (int) (camLeft / tileSize));
        int lastCol = Math.min(cols - 1, (int) ((camLeft + GameConfig.VIRTUAL_WIDTH) / tileSize) + 1);
        int firstRow = Math.max(0, (int) (camBottom / tileSize));
        int lastRow = Math.min(rows - 1, (int) ((camBottom + GameConfig.VIRTUAL_HEIGHT) / tileSize) + 1);

        for (int r = firstRow; r <= lastRow; r++) {
            for (int c = firstCol; c <= lastCol; c++) {
                int id = grid[r][c];
                if (id == TileType.AIR || id >= regions.length || regions[id] == null) continue;
                batch.draw(regions[id], c * tileSize, r * tileSize, tileSize, tileSize);
            }
        }
    }

    // ----- TileSoliditySource (contrato do engine de colisao) ----------------
    @Override public int getColumns() { return cols; }
    @Override public int getRows() { return rows; }
    @Override public int getTileSize() { return tileSize; }

    @Override
    public boolean isSolid(int col, int row) {
        return TileType.isSolid(get(col, row));
    }
}
