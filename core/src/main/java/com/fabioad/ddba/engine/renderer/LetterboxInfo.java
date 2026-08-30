package com.fabioad.ddba.engine.renderer;

/**
 * LetterboxInfo
 * =============================================================================
 * Geometria do integer scaling na janela real (Y-up para desenho em tela).
 */
public final class LetterboxInfo {
    public final int winW;
    public final int winH;
    public final int scale;
    public final int offsetX;
    public final int offsetY;
    public final int drawW;
    public final int drawH;

    public LetterboxInfo(int winW, int winH, int scale, int offsetX, int offsetY, int drawW, int drawH) {
        this.winW = winW;
        this.winH = winH;
        this.scale = scale;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.drawW = drawW;
        this.drawH = drawH;
    }

    /** Converte toque Android (Y para baixo) → coordenadas de tela Y-up. */
    public float touchToScreenY(int screenYFromTop) {
        return winH - screenYFromTop;
    }
}
