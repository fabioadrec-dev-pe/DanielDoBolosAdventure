package com.fabioad.ddba.game.player;

/**
 * PlayerState
 * =============================================================================
 * OBJETIVO:
 *   Enumerar os ESTADOS visuais/logicos do heroi. Cada estado escolhe uma
 *   animacao e pode alterar regras (ex.: CROUCH reduz movimento; DEAD tira o
 *   controle). E o coracao de uma pequena MAQUINA DE ESTADOS.
 *
 * PORTABILIDADE:
 *   - SNES: normalmente um byte "player_state" em RAM indexa uma tabela de
 *     ponteiros de rotina (uma sub-rotina por estado). Mesmo conceito.
 */
public enum PlayerState {
    IDLE,     // parado
    WALK,     // andando
    RUN,      // correndo
    JUMP,     // subindo
    FALL,     // caindo
    CROUCH,   // abaixado
    LOOK_UP,  // olhando para cima
    HURT,     // tomando dano
    DEAD,     // morte
    VICTORY   // vitoria
}
