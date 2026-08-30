package com.fabioad.ddba.engine.collision;

/**
 * TileSoliditySource
 * =============================================================================
 * OBJETIVO DA INTERFACE:
 *   Fornecer ao resolvedor de colisao (que vive no ENGINE) a informacao de quais
 *   tiles sao SOLIDOS, SEM que o engine conheca a classe de mapa do GAME.
 *
 * DECISAO DE ARQUITETURA (inversao de dependencia - "D" de SOLID):
 *   O engine define o CONTRATO; o game (TileMap) o IMPLEMENTA. Assim o engine
 *   nunca importa pacotes de game -> evita dependencia circular e mantem o
 *   engine reutilizavel em outros jogos.
 *
 * PORTABILIDADE:
 *   - SNES: a "solidez" viria de uma tabela de propriedades por tile (metadados),
 *     tipicamente indexada pelo numero do tile no tilemap do BG.
 */
public interface TileSoliditySource {

    /** Numero de colunas do mapa. */
    int getColumns();

    /** Numero de linhas do mapa. */
    int getRows();

    /** Tamanho do tile em pixels. */
    int getTileSize();

    /** True se o tile na coluna/linha bloqueia movimento. Fora do mapa = trata caller. */
    boolean isSolid(int col, int row);
}
