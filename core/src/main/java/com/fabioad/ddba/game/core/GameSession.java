package com.fabioad.ddba.game.core;

import com.fabioad.ddba.engine.core.GameConfig;

/**
 * GameSession
 * =============================================================================
 * OBJETIVO DA CLASSE:
 *   Guardar o ESTADO GLOBAL de uma partida em andamento: vidas, moedas, pontos,
 *   fase atual, tempo e checkpoint. E o "save em memoria" que sobrevive entre as
 *   telas (fase -> game over -> proxima fase).
 *
 * DECISOES DE ARQUITETURA:
 *   - Separado da GameContext (engine): isto e REGRA DE JOGO, nao servico do engine.
 *   - Metodos de mutacao expressivos (addCoin, loseLife...) centralizam as regras
 *     (ex.: 100 moedas = vida extra), evitando duplicacao pelas telas.
 *
 * PORTABILIDADE:
 *   - SNES: estes campos morariam em enderecos fixos da WRAM (ex.: $7E0000+).
 *     "addCoin" seria uma rotina que incrementa BCD e checa vida extra.
 */
public final class GameSession {

    private int lives;
    private int coins;
    private long score;
    private int stageIndex;      // 0..TOTAL_STAGES-1
    private float stageTime;     // segundos restantes na fase
    private int checkpointCol;   // coluna do ultimo checkpoint (tile)

    public GameSession() {
        reset();
    }

    /** Reinicia para uma nova partida (novo jogo). */
    public void reset() {
        lives = GameConfig.START_LIVES;
        coins = 0;
        score = 0;
        stageIndex = 0;
        stageTime = 300f;
        checkpointCol = 0;
    }

    /** Adiciona uma moeda; a cada 100, concede vida extra (classico 16 bits). */
    public void addCoin() {
        coins++;
        addScore(100);
        if (coins >= 100) {
            coins -= 100;
            lives++;
        }
    }

    public void addScore(long amount) {
        score += amount;
    }

    /** Perde uma vida. Retorna true se ainda restam vidas (continua jogo). */
    public boolean loseLife() {
        lives--;
        return lives > 0;
    }

    public void nextStage() {
        stageIndex++;
        stageTime = 300f;
        checkpointCol = 0;
    }

    public boolean isLastStageCleared() {
        return stageIndex >= GameConfig.TOTAL_STAGES;
    }

    public void tickTime(float dt) {
        stageTime = Math.max(0f, stageTime - dt);
    }

    // ----- getters/setters ---------------------------------------------------
    public int getLives() { return lives; }
    public int getCoins() { return coins; }
    public long getScore() { return score; }
    public int getStageIndex() { return stageIndex; }
    public int getStageNumber() { return stageIndex + 1; }
    public float getStageTime() { return stageTime; }
    public int getCheckpointCol() { return checkpointCol; }
    public void setCheckpointCol(int col) { this.checkpointCol = col; }
    public void setStageTime(float t) { this.stageTime = t; }
    public void setLives(int lives) { this.lives = lives; }
    public void setStageIndex(int index) { this.stageIndex = index; }
}
