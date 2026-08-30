package com.fabioad.ddba.engine.assets;

/**
 * AssetPaths
 * =============================================================================
 * OBJETIVO DA CLASSE:
 *   Reunir, em UM lugar, todos os caminhos de arquivos de recursos (assets).
 *   Evita strings "soltas" pelo codigo (fonte unica da verdade) e facilita
 *   renomear/mover arquivos sem cacar referencias.
 *
 * CONVENCAO:
 *   Caminhos sao relativos a pasta "assets" (que o Gradle coloca no classpath),
 *   entao sao carregados via Gdx.files.internal(...).
 *
 * PORTABILIDADE:
 *   - SNES: nao existem "arquivos"; tudo vira bancos de dados na ROM. Estes
 *     nomes corresponderiam a rotulos (labels) de tabelas de tiles/paletas/BRR.
 */
public final class AssetPaths {

    // ----- SPRITES -----------------------------------------------------------
    public static final String PLAYER = "sprites/daniel.png";
    public static final String COIN = "sprites/coin.png";
    public static final String ENEMY_WALKER = "sprites/enemy_walker.png";
    public static final String ENEMY_FLYER = "sprites/enemy_flyer.png";
    public static final String ENEMY_FAST = "sprites/enemy_fast.png";
    public static final String ENEMY_TANK = "sprites/enemy_tank.png";
    public static final String BOSS = "sprites/boss.png";
    /** Castelo / barraca de fim de fase (estilo boteco de praia). */
    public static final String CASTLE = "sprites/castle.png";

    // ----- TILES -------------------------------------------------------------
    public static final String TILESET = "tiles/tileset.png";

    // ----- FUNDOS (um por fase, para identidade visual propria) --------------
    public static final String[] BACKGROUNDS = {
            "textures/bg_stage1.png",
            "textures/bg_stage2.png",
            "textures/bg_stage3.png",
            "textures/bg_stage4.png",
            "textures/bg_stage5.png",
    };

    /** Fundo pixelizado do menu / tela de ruas (Daniel). */
    public static final String MENU_BG = "textures/menu_bg.png";

    /** Foto final do iate (telas de ending). */
    public static final String ENDING_BG = "textures/ending_bg.jpg";

    /** Fonte pixel (BMFont) — nitida no canvas 256x224. */
    public static final String FONT_PIXEL = "fonts/pixel.fnt";

    // ----- MUSICA ------------------------------------------------------------
    public static final String MUSIC_MENU = "music/menu.ogg";
    public static final String MUSIC_STAGE = "music/stage.ogg";
    public static final String MUSIC_BOSS = "music/boss.ogg";
    /** Fanfarra curta (limpeza de fases 1-4); mantida para usos gerais. */
    public static final String MUSIC_VICTORY = "music/victory.ogg";
    /** Tema de creditos apos o stage 5 (convertido de victory.mid). */
    public static final String MUSIC_VICTORY_FINAL = "music/victory_final.ogg";
    public static final String MUSIC_GAMEOVER = "music/gameover.ogg";

    // ----- EFEITOS SONOROS (SFX) --------------------------------------------
    public static final String SFX_JUMP = "sfx/jump.wav";
    public static final String SFX_COIN = "sfx/coin.wav";
    public static final String SFX_HURT = "sfx/hurt.wav";
    public static final String SFX_DEFEAT = "sfx/defeat.wav";
    public static final String SFX_BREAK = "sfx/break.wav";
    public static final String SFX_VICTORY = "sfx/victory.wav";
    public static final String SFX_GAMEOVER = "sfx/gameover.wav";
    public static final String SFX_MENU = "sfx/menu.wav";
    public static final String SFX_SELECT = "sfx/select.wav";
    public static final String SFX_BOSS = "sfx/boss.wav";

    private AssetPaths() {
    }
}
