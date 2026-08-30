package com.fabioad.ddba.game.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.fabioad.ddba.engine.core.GameConfig;
import com.fabioad.ddba.game.core.GameSession;

/**
 * Hud (Heads-Up Display)
 * =============================================================================
 * OBJETIVO DA CLASSE:
 *   Desenhar as informacoes fixas na tela: VIDAS, MOEDAS, PONTUACAO, TEMPO e FASE.
 *   E desenhada com a projecao ESTATICA da HUD (nao rola com a camera).
 *
 * DECISOES DE ARQUITETURA:
 *   - Le apenas a GameSession (fonte da verdade do placar); nao guarda estado.
 *   - A fonte e pixel BMFont em escala 1x (nitida no canvas 256x224).
 *     TODO(port): no SNES isto vira tiles de digitos na VRAM.
 *
 * PORTABILIDADE:
 *   - SNES: a HUD e tipicamente um BG separado com scroll travado, ou uma faixa
 *     de tiles no topo; os numeros sao tiles de digitos atualizados por VBlank.
 */
public final class Hud {

    private final BitmapFont font;

    public Hud(BitmapFont font) {
        this.font = font;
    }

    public void draw(SpriteBatch batch, GameSession session, String stageName) {
        float prevScaleX = font.getData().scaleX;
        float prevScaleY = font.getData().scaleY;
        font.getData().setScale(1f); // HUD no mesmo tamanho dos menus (fonte pixel base)
        font.setUseIntegerPositions(true);
        font.setColor(Color.WHITE);

        int top = GameConfig.VIRTUAL_HEIGHT - 2;

        // Linha superior: vidas | moedas | pontuacao
        font.draw(batch, "DANIEL x" + session.getLives(), 4, top);
        font.draw(batch, "PEIXES " + pad(session.getCoins(), 2), 92, top);
        font.draw(batch, "PONTOS " + pad((int) session.getScore(), 6), 168, top);

        // Linha inferior da HUD: fase | tempo
        font.draw(batch, "FASE " + session.getStageNumber() + "/" + GameConfig.TOTAL_STAGES, 4, top - 12);
        font.draw(batch, "TEMPO " + pad((int) Math.ceil(session.getStageTime()), 3), 168, top - 12);

        font.getData().setScale(prevScaleX, prevScaleY);
    }

    private static String pad(int value, int digits) {
        String s = Integer.toString(Math.max(0, value));
        while (s.length() < digits) s = "0" + s;
        return s;
    }
}
