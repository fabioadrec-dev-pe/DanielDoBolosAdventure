package com.fabioad.ddba.game.core;

import com.fabioad.ddba.engine.core.AbstractScreen;

/**
 * BaseGameScreen
 * =============================================================================
 * OBJETIVO DA CLASSE:
 *   Base comum a todas as telas ESPECIFICAS DO JOGO. Estende a AbstractScreen do
 *   engine (que traz o loop de passo fixo + pixel-perfect) e adiciona acesso
 *   pratico ao DanielGame (troca de telas) e a GameSession (estado da partida).
 *
 * DECISAO DE ARQUITETURA:
 *   Mantem a AbstractScreen do engine "pura" (sem conhecer regras do jogo) e
 *   concentra aqui o que e especifico do game. Fiel a separacao engine/game.
 */
public abstract class BaseGameScreen extends AbstractScreen {

    protected final DanielGame game;
    protected final GameSession session;

    protected BaseGameScreen(DanielGame game) {
        super(game.getContext());
        this.game = game;
        this.session = game.getSession();
    }
}
