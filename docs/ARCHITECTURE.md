# Guia de Arquitetura

Este documento descreve **como o jogo esta organizado**, o fluxo da engine e do
gameplay, e as decisoes que mantem o codigo modular e portavel.

## Principios

1. **Separacao Engine × Game.** `engine` e um motor 2D reutilizavel; `game` e o
   jogo especifico. Regra de ouro: **`engine` nunca importa `game`**. A
   comunicacao no sentido inverso (engine precisando de dados do jogo) usa
   *interfaces* definidas no engine (ex.: `TileSoliditySource`) — inversao de
   dependencia (o "D" de SOLID).
2. **Responsabilidade unica** por pacote/classe (SRP).
3. **Passo fixo deterministico** (60 Hz) para fisica estavel.
4. **Sem dependencias circulares** — garantido pela direcao de dependencia entre
   pacotes.
5. **Composition root unico** (`DanielGame`) cria e injeta servicos.

## Camadas

```mermaid
flowchart TD
    APP["app / launcher\nDesktopLauncher (main)"] --> GAME
    GAME["game (jogo)\nstages, player, enemies, maps, hud, core"] --> ENGINE
    ENGINE["engine (motor reutilizavel)\ncore, renderer, camera, input,\ncollision, audio, assets, ui"]
    ENGINE -. "interfaces (ex.: TileSoliditySource)" .-> GAME
```

- **app/launcher**: unica parte dependente de plataforma (janela LWJGL3).
- **game**: regras do jogo; conhece `engine`.
- **engine**: nao conhece o jogo; oferece servicos e contratos.

## Fluxograma da Engine (loop principal)

Todo `AbstractScreen` executa o mesmo laco com **acumulador de tempo** (fixed
timestep) e o pipeline **pixel-perfect**:

```mermaid
flowchart TD
    A["render(delta) chamado pelo LibGDX"] --> B["input.update() (snapshot)"]
    B --> C["accumulator += min(delta, MAX_FRAME_TIME)"]
    C --> D{"accumulator >= FIXED_TIMESTEP?"}
    D -- sim --> E["update(FIXED_TIMESTEP)"]
    E --> F["accumulator -= FIXED_TIMESTEP"]
    F --> D
    D -- nao --> G["render.beginWorld(clearColor) -> FBO 256x224"]
    G --> H["draw() (mundo + HUD)"]
    H --> I["render.endWorld()"]
    I --> J["render.present() -> upscale inteiro + letterbox"]
```

**Por que assim?** A logica roda sempre em passos de 1/60 s (deterministica,
independente do FPS de video). A renderizacao acontece uma vez por quadro no
canvas virtual e so entao e ampliada para a janela real, garantindo pixels
nitidos e proporcao correta.

## Fluxograma do Gameplay (PlayScreen)

```mermaid
flowchart TD
    S["show(): StageFactory.build(stage)\ncria Player, inimigos, moedas, camera"] --> P["Estado PLAYING"]
    P --> T["tickTime; player.update; enemies.update; coins.update"]
    T --> COL["Colisoes: moedas, inimigos, checkpoints, objetivo"]
    COL --> Q{"jogador x inimigo"}
    Q -- "caindo e pes acima" --> ST["PISAO: derrota/fere + quica + pontos"]
    Q -- "de lado/por baixo" --> DMG["DANO: player.hurt()"]
    DMG --> DIE["Estado DYING"]
    COL --> GOAL{"chegou ao objetivo?"}
    GOAL -- sim --> CLR["Estado CLEAR: pose de vitoria + bonus"]
    DIE --> LL{"restam vidas?"}
    LL -- sim --> RS["respawn no checkpoint -> PLAYING"]
    LL -- nao --> GO["GameOverScreen"]
    CLR --> NX{"ultima fase?"}
    NX -- nao --> NXT["proxima fase (nova PlayScreen)"]
    NX -- sim --> WIN["VictoryScreen (creditos)"]
```

## Fluxo de telas (state machine de alto nivel)

```mermaid
stateDiagram-v2
    [*] --> Boot
    Boot --> Title: assets carregados
    Title --> Presentation: START
    Presentation --> Menu: START (fade out)
    Menu --> Play: NOVO JOGO
    Play --> Play: proxima fase
    Play --> GameOver: sem vidas
    Play --> Victory: 5a fase concluida
    GameOver --> Menu: START
    Victory --> Menu: START
```

## Colisao (algoritmo)

- **Tile × entidade:** `TileCollisionResolver.move(box, dx, dy, world)` resolve
  **um eixo por vez** (X depois Y). Ao encontrar um tile solido, empurra a caixa
  para fora e zera a velocidade do eixo. Retorna flags (`onGround`, `hitCeiling`,
  `hitLeft`, `hitRight`). A velocidade de queda e limitada
  (`MAX_FALL_SPEED`) para evitar *tunneling*.
- **Entidade × entidade:** sobreposicao de `AABB` (teorema dos eixos separadores).

## Injecao de dependencias

`DanielGame` (composition root) cria o `GameContext` (servicos de engine) e a
`GameSession` (estado da partida) e os injeta nas telas via `BaseGameScreen`.
Nao ha singletons globais, o que facilita testes e portes.
