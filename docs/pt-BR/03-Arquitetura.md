# 03 — Guia da Arquitetura

## Princípios

1. **Separação Engine × Game.** O pacote `engine` é o motor 2D reutilizável; o
   pacote `game` contém a jogabilidade. Regra de ouro: **`engine` nunca importa
   `game`**.
2. **Responsabilidade única** por pacote e classe (SRP).
3. **Passo fixo determinístico (60 Hz)** para física estável.
4. **Sem dependências circulares.**
5. **Composition root único** (`DanielGame`) cria e injeta os serviços.

## Camadas

```mermaid
flowchart TD
    APP["app / launcher<br/>DesktopLauncher (main)"] --> GAME
    GAME["game (jogo)<br/>stages, player, enemies, maps, hud"] --> ENGINE
    ENGINE["engine (motor)<br/>core, renderer, camera, input,<br/>collision, audio, assets, ui"]
    ENGINE -. "interfaces<br/>(ex.: TileSoliditySource)" .-> GAME
```

| Camada | Papel |
|--------|-------|
| `app` | Única parte dependente de plataforma (janela LWJGL3) |
| `game` | Regras do jogo; usa o `engine` |
| `engine` | Serviços reutilizáveis; não conhece o jogo |

Quando o motor precisa de dados do jogo (ex.: “este tile é sólido?”), usa uma
**interface** definida no engine (`TileSoliditySource`). O jogo implementa o
contrato — isso é **inversão de dependência** (o “D” de SOLID).

## Fluxograma da Engine

Todo `AbstractScreen` executa o mesmo laço:

```mermaid
flowchart TD
    A["render(delta)"] --> B["input.update()"]
    B --> C["accumulator += delta"]
    C --> D{"accumulator >= 1/60?"}
    D -- sim --> E["update(1/60)"]
    E --> F["accumulator -= 1/60"]
    F --> D
    D -- não --> G["Desenha no FBO 256×224"]
    G --> H["draw()"]
    H --> I["present() → upscale inteiro + letterbox"]
```

**Por quê?** A lógica roda sempre a 60 passos por segundo (como um console NTSC).
A renderização ocorre uma vez por quadro no canvas virtual e depois é ampliada
para a janela real, mantendo pixels nítidos.

## Fluxograma do Gameplay

```mermaid
flowchart TD
    S["show(): carrega fase"] --> P["PLAYING"]
    P --> COL["Colisões: moedas, inimigos, checkpoints, objetivo"]
    COL --> Q{"jogador × inimigo"}
    Q -- "caindo e pés acima" --> ST["Pisão: derrota/fere + quica"]
    Q -- "de lado / por baixo" --> DMG["Dano → DYING"]
    COL --> GOAL{"chegou ao objetivo?"}
    GOAL -- sim --> CLR["CLEAR → próxima fase ou vitória"]
    DMG --> LL{"ainda há vidas?"}
    LL -- sim --> RS["Respawn no checkpoint"]
    LL -- não --> GO["Game Over"]
```

## Fluxo de telas

```mermaid
stateDiagram-v2
    [*] --> Boot
    Boot --> Title: assets carregados
    Title --> Presentation: START
    Presentation --> Menu: START
    Menu --> Play: Novo jogo
    Play --> Play: próxima fase
    Play --> GameOver: sem vidas
    Play --> Victory: 5ª fase concluída
    GameOver --> Menu
    Victory --> Menu
```

## Colisão

- **Tiles:** `TileCollisionResolver` move a caixa um eixo por vez (X depois Y).
  Ao bater em tile sólido, encosta a caixa e zera a velocidade daquele eixo.
- **Entidades:** sobreposição de `AABB` (caixas alinhadas aos eixos).
- A velocidade de queda é limitada (`MAX_FALL_SPEED`) para evitar *tunneling*.

## Decisões importantes

| Decisão | Motivo |
|---------|--------|
| Módulo Gradle único + pacotes bem separados | Build simples; evita dependências circulares |
| Fixed timestep | Física determinística |
| Canvas 256×224 + upscale inteiro | Visual fiel ao SNES em monitores modernos |
| Sem singletons | Facilita testes e portes |
