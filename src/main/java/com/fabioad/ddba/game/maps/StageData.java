package com.fabioad.ddba.game.maps;

import com.badlogic.gdx.utils.Array;

/**
 * StageData
 * =============================================================================
 * OBJETIVO DA CLASSE:
 *   Agregar TUDO que define uma fase: o mapa de tiles, o ponto inicial do jogador,
 *   a posicao do objetivo (fim), os spawns de inimigos, moedas, checkpoints, o
 *   indice do fundo (identidade visual) e metadados (nome, se e fase de chefe).
 *
 * DECISAO DE ARQUITETURA:
 *   Estrutura de dados "burra" (POJO) produzida pela StageFactory e consumida pela
 *   PlayScreen. Separar DADOS (o que a fase e) de COMPORTAMENTO (como se joga)
 *   facilita, no futuro, carregar fases de arquivos externos (Tiled/JSON) sem
 *   mexer na jogabilidade.
 *
 * PORTABILIDADE:
 *   - SNES: equivale a um "cabecalho de fase" na ROM apontando para o tilemap,
 *     a lista de objetos (OBJs/inimigos) e o indice de paleta/musica.
 */
public final class StageData {

    /** Descricao de um spawn de inimigo. */
    public static final class EnemySpawn {
        public final String type; // "walker","flyer","fast","tank","boss"
        public final float x, y;
        public EnemySpawn(String type, float x, float y) {
            this.type = type; this.x = x; this.y = y;
        }
    }

    /** Posicao 2D simples (evita depender de Vector2 aqui). */
    public static final class Point {
        public final float x, y;
        public Point(float x, float y) { this.x = x; this.y = y; }
    }

    public TileMap map;
    public float playerStartX;
    public float playerStartY;
    public float goalX;
    public float goalY;
    public int backgroundIndex;
    public String name = "";
    public boolean bossStage;

    public final Array<EnemySpawn> enemies = new Array<>();
    public final Array<Point> coins = new Array<>();
    public final Array<Point> checkpoints = new Array<>();
    public final Array<Point> hiddenItems = new Array<>();
}
