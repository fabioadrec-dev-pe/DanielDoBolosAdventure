package com.fabioad.ddba.engine.core;

import com.badlogic.gdx.utils.Disposable;
import com.fabioad.ddba.engine.assets.Assets;
import com.fabioad.ddba.engine.audio.AudioManager;
import com.fabioad.ddba.engine.input.GameInput;
import com.fabioad.ddba.engine.renderer.RenderContext;

/**
 * GameContext
 * =============================================================================
 * OBJETIVO DA CLASSE:
 *   Reunir os SERVICOS globais do ENGINE (renderizacao, assets, audio, entrada)
 *   em um unico objeto passado as telas. E um "Service Locator" enxuto.
 *
 * DECISOES DE ARQUITETURA:
 *   - Injecao por composicao: as telas recebem o GameContext e usam os servicos,
 *     em vez de acessarem singletons globais. Facilita testes e portes.
 *   - Contem SOMENTE servicos de engine (nao conhece regras do jogo), preservando
 *     a regra "engine nunca depende de game".
 *
 * PORTABILIDADE:
 *   - Em C, este objeto viraria uma "struct de contexto" passada por ponteiro a
 *     todas as rotinas (padrao muito comum em engines C, ex.: raylib/allegro).
 */
public final class GameContext implements Disposable {

    public final RenderContext render;
    public final Assets assets;
    public final AudioManager audio;
    public final GameInput input;

    public GameContext() {
        this.audio = new AudioManager();
        this.render = new RenderContext();
        this.assets = new Assets(audio);
        this.input = new GameInput();
    }

    @Override
    public void dispose() {
        // Ordem inversa a criacao dos recursos pesados.
        assets.dispose();
        render.dispose();
        audio.dispose();
    }
}
