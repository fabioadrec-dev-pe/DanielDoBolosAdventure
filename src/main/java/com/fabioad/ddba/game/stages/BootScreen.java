package com.fabioad.ddba.game.stages;

import com.badlogic.gdx.graphics.Color;
import com.fabioad.ddba.game.core.BaseGameScreen;
import com.fabioad.ddba.game.core.DanielGame;

/**
 * BootScreen
 * =============================================================================
 * OBJETIVO DA CLASSE:
 *   Primeira tela do jogo: CARREGA todos os assets (texturas, animacoes, SFX) e,
 *   em seguida, transiciona para a tela de titulo. Isola o carregamento do resto.
 *
 * DECISAO DE ARQUITETURA:
 *   O carregamento e feito no primeiro tick logico e a troca de tela no tick
 *   seguinte, garantindo um quadro limpo antes da transicao (evita "flash").
 *   Para projetos maiores, aqui entraria uma barra de progresso com AssetManager
 *   assincrono.
 *
 * PORTABILIDADE:
 *   - SNES: equivale a fase de "init" que copia tiles/paletas/BRR da ROM para a
 *     VRAM/ARAM antes de mostrar a tela-titulo.
 */
public final class BootScreen extends BaseGameScreen {

    private int ticks;

    public BootScreen(DanielGame game) {
        super(game);
    }

    @Override
    protected Color clearColor() {
        return Color.BLACK;
    }

    @Override
    protected void update(float dt) {
        if (ticks == 0) {
            ctx.assets.load();
        }
        ticks++;
        if (ticks >= 2) {
            game.changeScreen(new TitleScreen(game));
        }
    }

    @Override
    protected void draw() {
        // Quadro preto durante o carregamento (rapido).
    }
}
