package com.fabioad.ddba.game.maps;

import com.fabioad.ddba.engine.core.GameConfig;
import com.fabioad.ddba.game.tiles.TileType;

import java.util.Random;

/**
 * StageFactory
 * =============================================================================
 * OBJETIVO DA CLASSE:
 *   Construir programaticamente as 5 fases (TileMap + spawns), cada uma com
 *   identidade visual, obstaculos exclusivos, dificuldade crescente, caminhos
 *   secretos e itens escondidos.
 *
 * DECISAO DE ARQUITETURA:
 *   Geracao PROCEDURAL DETERMINISTICA (seed = indice da fase). Vantagens:
 *     - Nao depende de arquivos de mapa externos (o jogo roda "de fabrica");
 *     - Reproduzivel (mesma fase sempre igual);
 *     - Facil de evoluir para carregar mapas do Tiled/JSON depois (basta trocar
 *       esta fabrica por um "loader"), sem tocar na jogabilidade.
 *   Convencao de grade: grid[row][col], row 0 = base (Y-up).
 *
 * PORTABILIDADE:
 *   - SNES: as fases seriam dados na ROM (nao geradas em runtime, por custo de CPU).
 *     Esta fabrica documenta a ESTRUTURA que esses dados teriam.
 */
public final class StageFactory {

    private static final int ROWS = 14; // 224 / 16

    private static final String[] NAMES = {
            "Fase 1 - Orla de Brasilia Teimosa",
            "Fase 2 - Ruas do Bairro",
            "Fase 3 - Cais e Jangadas",
            "Fase 4 - Ferro-velho",
            "Fase 5 - Castelo do Chefe",
    };

    private StageFactory() {
    }

    /** Constroi a fase de indice 0..4. */
    public static StageData build(int stageIndex) {
        int ts = GameConfig.TILE_SIZE;
        boolean boss = (stageIndex == GameConfig.TOTAL_STAGES - 1);

        // Largura cresce com a fase (dificuldade/duracao crescentes).
        int cols = boss ? 64 : (96 + stageIndex * 24);
        int[][] g = new int[ROWS][cols];

        Random rnd = new Random(1000L + stageIndex);
        int groundH = 2;              // altura do chao base (linhas 0..1)
        int topRow = groundH - 1;     // linha da superficie (GRASS)

        StageData data = new StageData();
        data.name = NAMES[stageIndex];
        data.backgroundIndex = stageIndex;
        data.bossStage = boss;

        // 1) Chao base contínuo.
        for (int c = 0; c < cols; c++) {
            g[topRow][c] = TileType.GRASS;
            for (int r = 0; r < topRow; r++) g[r][c] = TileType.DIRT;
        }

        // 2) Pits (buracos) - nao na fase de chefe. Dificuldade -> mais/maiores.
        boolean[] pit = new boolean[cols];
        if (!boss) {
            int pitCount = 2 + stageIndex * 2;
            for (int i = 0; i < pitCount; i++) {
                int len = 2 + rnd.nextInt(2 + stageIndex); // 2..(3+idx)
                int start = 10 + rnd.nextInt(Math.max(1, cols - 25));
                for (int c = start; c < Math.min(cols - 8, start + len); c++) {
                    for (int r = 0; r <= topRow; r++) g[r][c] = TileType.AIR;
                    pit[c] = true;
                    // Espinhos no fundo do pit a partir da fase 3 (mais punitivo).
                    if (stageIndex >= 2) g[0][c] = TileType.SPIKE;
                }
            }
        }

        // 3) Plataformas flutuantes + moedas em cima.
        int platforms = boss ? 2 : (4 + stageIndex * 2);
        for (int i = 0; i < platforms; i++) {
            int len = 3 + rnd.nextInt(3);
            int row = 4 + rnd.nextInt(5); // altura variada
            int start = 8 + rnd.nextInt(Math.max(1, cols - 16));
            for (int c = start; c < Math.min(cols - 4, start + len); c++) {
                g[row][c] = TileType.PLATFORM;
                // moeda logo acima da plataforma
                data.coins.add(new StageData.Point(c * ts, (row + 1) * ts));
            }
        }

        // 4) Fileiras de moedas sobre o chao (recompensa por percorrer).
        for (int c = 6; c < cols - 6; c += 6) {
            if (!pit[c]) data.coins.add(new StageData.Point(c * ts, (topRow + 2) * ts));
        }

        // 5) Item escondido (caminho secreto): moeda no alto, canto superior.
        data.hiddenItems.add(new StageData.Point((cols - 12) * ts, (ROWS - 3) * ts));
        data.coins.add(new StageData.Point((cols - 12) * ts, (ROWS - 3) * ts));

        // 6) Inimigos: tipos e quantidade crescem com a fase.
        if (boss) {
            // Arena de chefe: um chefe grande no meio-direita.
            data.enemies.add(new StageData.EnemySpawn("boss", (cols - 20) * ts, groundH * ts));
            data.enemies.add(new StageData.EnemySpawn("walker", (cols / 2) * ts, groundH * ts));
        } else {
            int enemyCount = 3 + stageIndex * 2;
            String[] pool = enemyPoolFor(stageIndex);
            for (int i = 0; i < enemyCount; i++) {
                int c = 12 + rnd.nextInt(Math.max(1, cols - 20));
                if (pit[c]) continue;
                String type = pool[rnd.nextInt(pool.length)];
                float ey = "flyer".equals(type) ? (topRow + 4) * ts : groundH * ts;
                data.enemies.add(new StageData.EnemySpawn(type, c * ts, ey));
            }
        }

        // 7) Checkpoints em ~1/3 e ~2/3 (colunas com chao).
        addCheckpoint(data, g, pit, cols / 3, ts, groundH);
        addCheckpoint(data, g, pit, (2 * cols) / 3, ts, groundH);

        // 8) Objetivo (bandeira) um pouco antes do fim; sobra espaco para a barraca/"castelo".
        int goalCol = cols - 10;
        data.goalX = goalCol * ts;
        data.goalY = groundH * ts;

        // 9) Inicio do jogador (coluna 2, sobre o chao).
        data.playerStartX = 2 * ts;
        data.playerStartY = groundH * ts;

        data.map = new TileMap(g);
        return data;
    }

    private static void addCheckpoint(StageData data, int[][] g, boolean[] pit, int col, int ts, int groundH) {
        col = Math.max(4, Math.min(g[0].length - 5, col));
        if (pit[col]) col++; // desloca se cair num buraco
        data.checkpoints.add(new StageData.Point(col * ts, groundH * ts));
    }

    /** Define quais tipos de inimigo aparecem em cada fase (progressao). */
    private static String[] enemyPoolFor(int stageIndex) {
        switch (stageIndex) {
            case 0: return new String[]{"walker"};
            case 1: return new String[]{"walker", "flyer"};
            case 2: return new String[]{"walker", "flyer", "fast"};
            case 3: return new String[]{"walker", "fast", "tank"};
            default: return new String[]{"walker", "flyer", "fast", "tank"};
        }
    }
}
