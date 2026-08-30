package com.fabioad.ddba.engine.core;

/**
 * GameConfig
 * =============================================================================
 * OBJETIVO DA CLASSE:
 *   Centralizar todas as CONSTANTES globais do jogo (resolucao, fisica, tempo,
 *   metadados). Uma unica fonte da verdade evita "numeros magicos" espalhados
 *   pelo codigo e facilita ajustes finos de jogabilidade.
 *
 * DECISOES DE ARQUITETURA:
 *   - A resolucao base e 256x224, exatamente a resolucao classica do SNES (NTSC).
 *     Todo o jogo e desenhado nesse "canvas" virtual e depois escalado por um
 *     numero INTEIRO ate a janela real (integer scaling / pixel-perfect).
 *   - Constantes de fisica sao expressas em "pixels por segundo" (px/s) e
 *     "pixels por segundo^2" para independerem da taxa de quadros.
 *   - Usamos um passo de simulacao fixo (FIXED_TIMESTEP) para fisica deterministica.
 *
 * PORTABILIDADE:
 *   - SNES (65816): a resolucao 256x224 e nativa; nao ha "escala inteira" pois a
 *     saida ja e a tela. As constantes de fisica virariam tabelas em ponto-fixo
 *     (ex.: 8.8 fixed-point) pois o 65816 nao tem FPU.
 *   - Mega Drive (68000): resolucao 320x224; ajustar VIRTUAL_WIDTH.
 *   - PC/Android/Switch: manter 256x224 virtual e escalar; em telas widescreen,
 *     usar barras laterais (pillarbox) para preservar o aspecto 8:7/4:3.
 *
 * OBSERVACAO: classe apenas de constantes -> construtor privado (nao instanciar).
 */
public final class GameConfig {

    /** Nome exibido do jogo (janela, creditos, instaladores). */
    public static final String TITLE = "Daniel do Bolo's Adventure";

    /** Autor/creditos. */
    public static final String AUTHOR = "fabio_ad";

    // ----- RESOLUCAO VIRTUAL (o "mundo em pixels" que desenhamos) -----------
    /** Largura do canvas virtual (SNES NTSC). */
    public static final int VIRTUAL_WIDTH = 256;
    /** Altura do canvas virtual (SNES NTSC). */
    public static final int VIRTUAL_HEIGHT = 224;

    // ----- JANELA INICIAL (desktop) -----------------------------------------
    /** Escala inicial da janela desktop (256*3 x 224*3). */
    public static final int DEFAULT_SCALE = 3;

    // ----- TEMPO / SIMULACAO -------------------------------------------------
    /**
     * Passo fixo de simulacao (segundos). 1/60 = 60 updates logicos por segundo,
     * casando com o refresh classico de 60 Hz. Garante fisica deterministica
     * independentemente da taxa de renderizacao.
     */
    public static final float FIXED_TIMESTEP = 1f / 60f;

    /** Limite de acumulo para evitar "espiral da morte" quando ha lag. */
    public static final float MAX_FRAME_TIME = 0.25f;

    // ----- FISICA (valores base; ajustaveis por jogabilidade) ---------------
    /** Tamanho do tile em pixels (padrao 16x16, classico 16 bits). */
    public static final int TILE_SIZE = 16;

    /** Gravidade em px/s^2 (puxa entidades para baixo). */
    public static final float GRAVITY = 900f;

    /** Velocidade de caminhada do jogador (px/s). */
    public static final float PLAYER_WALK_SPEED = 70f;

    /** Velocidade de corrida do jogador (px/s). */
    public static final float PLAYER_RUN_SPEED = 130f;

    /** Aceleracao horizontal ao pressionar direcao (px/s^2). */
    public static final float PLAYER_ACCEL = 600f;

    /** Desaceleracao (atrito) quando solta a direcao (px/s^2). */
    public static final float PLAYER_FRICTION = 700f;

    /** Impulso vertical inicial do pulo (px/s, negativo = para cima em coords Y-up). */
    public static final float PLAYER_JUMP_VELOCITY = 300f;

    /**
     * Gravidade reduzida enquanto o botao de pulo e mantido (pulo variavel):
     * quanto menor, mais alto o pulo ao segurar. Classico dos plataformas 16 bits.
     */
    public static final float PLAYER_JUMP_HOLD_GRAVITY = 500f;

    /** Velocidade maxima de queda (limite terminal), evita atravessar tiles. */
    public static final float MAX_FALL_SPEED = 480f;

    // ----- REGRAS DE JOGO ----------------------------------------------------
    /** Vidas iniciais. */
    public static final int START_LIVES = 3;

    /**
     * Duracao (segundos) de invencibilidade + piscada apos perder uma vida
     * (encostar em inimigo / apos respawn). Classico dos plataformas 16 bits.
     */
    public static final float PLAYER_INVINCIBLE_DURATION = 3f;

    /** Total de fases. */
    public static final int TOTAL_STAGES = 5;

    private GameConfig() {
        // Classe utilitaria: nao deve ser instanciada.
    }
}
