package com.fabioad.ddba.game.core;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.fabioad.ddba.engine.core.GameContext;
import com.fabioad.ddba.game.stages.BootScreen;

/**
 * DanielGame
 * =============================================================================
 * OBJETIVO DA CLASSE:
 *   E o ponto central da aplicacao (subclasse de com.badlogic.gdx.Game). Cria os
 *   servicos do engine (GameContext), o estado da partida (GameSession) e gerencia
 *   a TROCA de telas (setScreen). Vive no pacote GAME pois conhece engine e game.
 *
 * DECISOES DE ARQUITETURA:
 *   - "Composition root": e o unico lugar que instancia servicos e sessao, e os
 *     injeta nas telas (via getters). Evita singletons globais.
 *   - Boot em duas fases: cria contexto -> BootScreen carrega assets -> Title.
 *
 * PORTABILIDADE:
 *   - Em C, isto seria a funcao main() + um "state machine" de telas por ponteiros
 *     de funcao (update/draw) — padrao comum em jogos de console.
 */
public final class DanielGame extends Game {

    private GameContext ctx;
    private GameSession session;

    @Override
    public void create() {
        this.ctx = new GameContext();
        this.session = new GameSession();
        // A tela de boot carrega os assets e depois vai para o titulo.
        setScreen(new BootScreen(this));
    }

    public GameContext getContext() {
        return ctx;
    }

    public GameSession getSession() {
        return session;
    }

    /** Troca de tela liberando a anterior (evita vazamento de recursos). */
    public void changeScreen(Screen next) {
        Screen previous = getScreen();
        setScreen(next);
        if (previous != null) {
            previous.dispose();
        }
    }

    @Override
    public void dispose() {
        if (getScreen() != null) {
            getScreen().dispose();
        }
        if (ctx != null) {
            ctx.dispose();
        }
    }
}
