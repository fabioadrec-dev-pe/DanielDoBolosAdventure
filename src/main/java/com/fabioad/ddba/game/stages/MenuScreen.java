package com.fabioad.ddba.game.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.fabioad.ddba.engine.assets.AssetPaths;
import com.fabioad.ddba.engine.core.GameConfig;
import com.fabioad.ddba.engine.input.GameInput;
import com.fabioad.ddba.engine.ui.TextUtil;
import com.fabioad.ddba.game.core.BaseGameScreen;
import com.fabioad.ddba.game.core.DanielGame;

/**
 * MenuScreen (Menu Principal)
 * =============================================================================
 * OBJETIVO DA CLASSE:
 *   Menu navegavel por setas (cima/baixo) e confirmacao (START). Opcoes: iniciar
 *   um novo jogo ou sair. Toca a musica de menu e reproduz SFX de navegacao.
 *
 *   Sequencia secreta (CIMA, BAIXO, ESQUERDA, DIREITA, PULO, CORRER) revela
 *   selecao de fase e vidas antes de iniciar o jogo.
 *
 * DECISAO DE ARQUITETURA:
 *   Menu orientado a dados: um array de rotulos + um indice selecionado. Adicionar
 *   opcoes e trivial. A acao de cada item e resolvida por um 'switch' no confirm().
 *
 * PORTABILIDADE:
 *   - SNES: o cursor e um OBJ (seta) reposicionado; a selecao le o joypad no VBlank.
 */
public final class MenuScreen extends BaseGameScreen {

    private static final GameInput.Action[] CHEAT_SEQUENCE = {
            GameInput.Action.UP,
            GameInput.Action.DOWN,
            GameInput.Action.LEFT,
            GameInput.Action.RIGHT,
            GameInput.Action.JUMP,
            GameInput.Action.RUN,
    };

    private static final int MIN_LIVES = 1;
    private static final int MAX_LIVES = 9;

    private int selected;
    private float time;

    private int cheatProgress;
    private boolean cheatUnlocked;
    private int selectedStage = 1;
    private int selectedLives = GameConfig.START_LIVES;

    public MenuScreen(DanielGame game) {
        super(game);
    }

    @Override
    public void show() {
        ctx.render.resetWorldCamera();
        ctx.audio.playMusic(AssetPaths.MUSIC_MENU, true);
    }

    @Override
    protected void update(float dt) {
        time += dt;
        GameInput in = ctx.input;

        // Atualiza o codigo secreto sem bloquear a navegacao do menu (setas).
        boolean swallowJump = updateCheatSequence(in);

        if (in.isPressed(GameInput.Action.UP)) {
            selected = (selected - 1 + optionCount()) % optionCount();
            ctx.audio.playSfx("menu");
        }
        if (in.isPressed(GameInput.Action.DOWN)) {
            selected = (selected + 1) % optionCount();
            ctx.audio.playSfx("menu");
        }

        if (cheatUnlocked) {
            adjustCheatOptions(in);
        }

        boolean confirmPressed = in.isPressed(GameInput.Action.START)
                || (in.isPressed(GameInput.Action.JUMP) && !swallowJump);
        if (confirmPressed) {
            ctx.audio.playSfx("select");
            confirm();
        }
    }

    /**
     * Avanca (ou reinicia) a sequencia secreta. Retorna true se o JUMP deste quadro
     * fez parte do codigo (nao deve confirmar "NOVO JOGO" / "SAIR").
     */
    private boolean updateCheatSequence(GameInput in) {
        if (cheatUnlocked) {
            return false;
        }

        GameInput.Action pressed = getHeroActionPressed(in);
        if (pressed == null) {
            return false;
        }

        boolean matched = pressed == CHEAT_SEQUENCE[cheatProgress];
        if (matched) {
            cheatProgress++;
            if (cheatProgress >= CHEAT_SEQUENCE.length) {
                cheatUnlocked = true;
                cheatProgress = 0;
                selected = Math.min(selected, optionCount() - 1);
                ctx.audio.playSfx("select");
            }
        } else {
            cheatProgress = (pressed == CHEAT_SEQUENCE[0]) ? 1 : 0;
        }

        // So engole JUMP quando ele era o proximo passo do codigo (ex.: 5o comando).
        return matched && pressed == GameInput.Action.JUMP;
    }

    private static GameInput.Action getHeroActionPressed(GameInput in) {
        for (GameInput.Action action : CHEAT_SEQUENCE) {
            if (in.isPressed(action)) {
                return action;
            }
        }
        return null;
    }

    private void adjustCheatOptions(GameInput in) {
        if (selected == 1) {
            if (in.isPressed(GameInput.Action.LEFT)) {
                selectedStage = Math.max(1, selectedStage - 1);
                ctx.audio.playSfx("menu");
            }
            if (in.isPressed(GameInput.Action.RIGHT)) {
                selectedStage = Math.min(GameConfig.TOTAL_STAGES, selectedStage + 1);
                ctx.audio.playSfx("menu");
            }
        } else if (selected == 2) {
            if (in.isPressed(GameInput.Action.LEFT)) {
                selectedLives = Math.max(MIN_LIVES, selectedLives - 1);
                ctx.audio.playSfx("menu");
            }
            if (in.isPressed(GameInput.Action.RIGHT)) {
                selectedLives = Math.min(MAX_LIVES, selectedLives + 1);
                ctx.audio.playSfx("menu");
            }
        }
    }

    private int optionCount() {
        return cheatUnlocked ? 4 : 2;
    }

    private String optionLabel(int index) {
        switch (index) {
            case 0:
                return "NOVO JOGO";
            case 1:
                return cheatUnlocked ? "FASE: " + selectedStage : "SAIR";
            case 2:
                return "VIDAS: " + selectedLives;
            case 3:
                return "SAIR";
            default:
                return "";
        }
    }

    private void confirm() {
        switch (selected) {
            case 0: // NOVO JOGO
                session.reset();
                if (cheatUnlocked) {
                    session.setStageIndex(selectedStage - 1);
                    session.setLives(selectedLives);
                }
                game.changeScreen(new PlayScreen(game));
                break;
            case 1:
                if (cheatUnlocked) {
                    break;
                }
                Gdx.app.exit();
                break;
            case 3: // SAIR (modo cheat)
                Gdx.app.exit();
                break;
            default:
                break;
        }
    }

    @Override
    protected void draw() {
        // Projecao estatica: apos a fase a worldCamera fica scrollada (tela preta).
        ctx.render.applyHudProjection();

        float w = GameConfig.VIRTUAL_WIDTH;
        float h = GameConfig.VIRTUAL_HEIGHT;

        drawMenuBackground(w, h);

        TextUtil.drawCentered(ctx.render.getBatch(), ctx.assets.getFont(),
                "DANIEL DO BOLO'S ADVENTURE", w / 2f, h * 0.82f, 0.5f, Color.GOLD);

        int count = optionCount();
        for (int i = 0; i < count; i++) {
            boolean sel = (i == selected);
            Color c = sel ? Color.YELLOW : Color.WHITE;
            String label = (sel ? "> " : "  ") + optionLabel(i) + (sel ? " <" : "");
            TextUtil.drawCentered(ctx.render.getBatch(), ctx.assets.getFont(),
                    label, w / 2f, h * 0.52f - i * 14, 0.5f, c);
        }

        if (cheatUnlocked && (selected == 1 || selected == 2)) {
            TextUtil.drawCentered(ctx.render.getBatch(), ctx.assets.getFont(),
                    "Esquerda/Direita: ajustar", w / 2f, 42, 0.4f, Color.LIGHT_GRAY);
        }

        TextUtil.drawCentered(ctx.render.getBatch(), ctx.assets.getFont(),
                "Setas: mover   ENTER/Z: confirmar", w / 2f, 30, 0.4f, Color.LIGHT_GRAY);
        TextUtil.drawCentered(ctx.render.getBatch(), ctx.assets.getFont(),
                "Mover: setas/AD   Correr: SHIFT/X   Pular: ESPACO/Z", w / 2f, 16, 0.4f, Color.GRAY);
    }

    /** Fundo tela cheia pixelizado + véu escuro para o texto continuar legível. */
    private void drawMenuBackground(float w, float h) {
        if (ctx.assets.menuBg != null) {
            ctx.render.getBatch().setColor(1f, 1f, 1f, 1f);
            ctx.render.getBatch().draw(ctx.assets.menuBg, 0, 0, w, h);
            // Véu escuro por cima (não atrapalha a leitura).
            ctx.render.fillRect(0, 0, w, h, new Color(0f, 0f, 0f, 0.45f));
        } else {
            ctx.render.fillRect(0, 0, w, h, new Color(0.04f, 0.06f, 0.12f, 1f));
        }
    }
}
