package com.fabioad.ddba.game.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.fabioad.ddba.engine.collision.AABB;

/**
 * Entity
 * =============================================================================
 * OBJETIVO DA CLASSE:
 *   Classe base de tudo que existe no mundo e se move/colide (jogador, inimigos).
 *   Reune o que TODA entidade tem: caixa de colisao (AABB), velocidade, direcao
 *   que encara, tempo de animacao e um estado "vivo".
 *
 * DECISAO DE ARQUITETURA:
 *   Heranca rasa e coesa (Entity -> Player/Enemy). Para projetos maiores, um ECS
 *   (Entity-Component-System) escalaria melhor; para um plataforma 16 bits, esta
 *   heranca simples e mais clara e didatica (e mais proxima de um porte em C).
 *
 * PORTABILIDADE:
 *   - SNES: cada entidade seria uma "struct" em RAM (x, y em 8.8, vx, vy, flags,
 *     ptr de animacao). O 'update' viraria uma rotina por tipo, chamada no loop.
 */
public abstract class Entity {

    /** Caixa de colisao/posicao (canto inferior-esquerdo, Y-up). */
    public final AABB box = new AABB();

    /** Velocidade em px/s. */
    public float vx, vy;

    /** Direcao horizontal que a entidade encara (para espelhar o sprite). */
    public boolean facingRight = true;

    /** Tempo acumulado para selecionar o quadro de animacao. */
    protected float stateTime;

    /** Entidade ativa no mundo? (false = remover/ignorar). */
    public boolean alive = true;

    protected Entity(float x, float y, float width, float height) {
        box.set(x, y, width, height);
    }

    /** Atualiza a logica da entidade (passo fixo). */
    public abstract void update(float dt);

    /** Desenha a entidade. batch ja esta ativo com a projecao do mundo. */
    public abstract void draw(SpriteBatch batch);

    public float getX() { return box.x; }
    public float getY() { return box.y; }
    public float getCenterX() { return box.centerX(); }
    public float getCenterY() { return box.centerY(); }
}
