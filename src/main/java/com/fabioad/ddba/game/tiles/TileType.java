package com.fabioad.ddba.game.tiles;

/**
 * TileType
 * =============================================================================
 * OBJETIVO DA CLASSE:
 *   Definir os TIPOS de tile e suas PROPRIEDADES (solido? perigoso? quebravel?).
 *   O mapa guarda apenas o ID numerico do tile (compacto); esta classe traduz o
 *   ID em comportamento. O ID tambem indexa a regiao grafica no tileset.png.
 *
 * DECISAO DE ARQUITETURA:
 *   Tabelas estaticas (arrays) indexadas pelo ID -> consulta O(1), sem objetos por
 *   tile. Esse padrao "tile properties table" e exatamente o usado em consoles
 *   16 bits (uma tabela de atributos por tile).
 *
 * PORTABILIDADE:
 *   - SNES: a solidez/perigo viriam de uma tabela de metadados paralela ao tilemap
 *     (ou dos bits de prioridade/atributo). O ID aqui = numero do tile na VRAM.
 */
public final class TileType {

    // IDs dos tiles (coincidem com a ordem no tileset.png).
    public static final int AIR = 0;
    public static final int GRASS = 1;   // topo de chao (solido)
    public static final int DIRT = 2;    // terra (solido)
    public static final int BRICK = 3;   // bloco quebravel (solido)
    public static final int PLATFORM = 4;// plataforma (solido)
    public static final int SPIKE = 5;   // espinho (perigo, nao solido)
    public static final int STONE = 6;   // pedra (solido)
    public static final int DECO = 7;    // decoracao (nao solido)

    public static final int COUNT = 8;

    // Tabelas de propriedades indexadas pelo ID.
    private static final boolean[] SOLID = new boolean[COUNT];
    private static final boolean[] HAZARD = new boolean[COUNT];
    private static final boolean[] BREAKABLE = new boolean[COUNT];

    static {
        SOLID[GRASS] = true;
        SOLID[DIRT] = true;
        SOLID[BRICK] = true;
        SOLID[PLATFORM] = true;
        SOLID[STONE] = true;

        HAZARD[SPIKE] = true;

        BREAKABLE[BRICK] = true;
    }

    public static boolean isSolid(int id) {
        return id >= 0 && id < COUNT && SOLID[id];
    }

    public static boolean isHazard(int id) {
        return id >= 0 && id < COUNT && HAZARD[id];
    }

    public static boolean isBreakable(int id) {
        return id >= 0 && id < COUNT && BREAKABLE[id];
    }

    private TileType() {
    }
}
