# 04 — Explicação de Cada Módulo

Os “módulos” do enunciado (core, graphics, renderer, animation, camera, input,
physics, collision, audio, music, sfx, maps, tiles, sprites, entities, enemies,
player, hud, ui, menus, save, stages, particles, shaders, assets, tools) estão
organizados como **pacotes Java** sob `engine.*` e `game.*`.

## Engine (`com.fabioad.ddba.engine`)

| Pacote | Responsabilidade | Classes principais |
|--------|------------------|--------------------|
| `core` | Loop, telas base, contexto, constantes | `GameConfig`, `GameContext`, `AbstractScreen` |
| `renderer` | Pipeline pixel-perfect (FBO + upscale) | `RenderContext` |
| `camera` | Câmera lateral com follow e clamp | `SideScrollerCamera` |
| `input` | Ações lógicas abstraídas do hardware | `GameInput` |
| `collision` | AABB + resolução contra tiles | `AABB`, `TileCollisionResolver`, `TileSoliditySource` |
| `audio` | Música (streaming) e SFX | `AudioManager` |
| `assets` | Carregar/fatiar texturas e animações | `Assets`, `AssetPaths` |
| `ui` | Texto centralizado para menus/créditos | `TextUtil` |

Observações:

- **animation:** usa `com.badlogic.gdx.graphics.g2d.Animation`, montada em `Assets`.
- **physics:** constantes em `GameConfig`; integração nas entidades; interação
  com o mundo via `collision`.
- **particles / shaders:** pastas reservadas em `assets/` para expansão futura.

## Game (`com.fabioad.ddba.game`)

| Pacote | Responsabilidade | Classes principais |
|--------|------------------|--------------------|
| `core` | Ponto central, sessão e base de telas | `DanielGame`, `GameSession`, `BaseGameScreen` |
| `stages` | Telas e progresso do jogo | `BootScreen`, `TitleScreen`, `PresentationScreen`, `MenuScreen`, `PlayScreen`, `GameOverScreen`, `VictoryScreen` |
| `player` | Herói: física, estados, animações | `Player`, `PlayerState` |
| `enemies` | IA por tipo | `Enemy`, `Walker`, `FastRunner`, `Tank`, `Flyer`, `Boss`, `EnemyFactory` |
| `entities` | Base de entidades e itens | `Entity`, `Coin` |
| `maps` | Dados e geração das fases | `TileMap`, `StageData`, `StageFactory` |
| `tiles` | Tipos e propriedades dos tiles | `TileType` |
| `hud` | Placar | `Hud` |
| `data` | Dados de ambientação | `BrasiliaTeimosaStreets` |
| `save` | Estado da partida em memória | `GameSession` (persistência em disco = TODO) |

## App / Launcher

| Pacote | Responsabilidade | Classe |
|--------|------------------|--------|
| `app` | Entrada desktop (janela LWJGL3) | `DesktopLauncher` |

É a **única** classe dependente de plataforma. Launchers futuros (Android, HTML)
reutilizam `DanielGame` sem alteração.

## Tools

Scripts em `tools/` (fora do código Java):

| Script | Função |
|--------|--------|
| `generate_assets.py` | Gera sprites, tiles e fundos (Pillow) |
| `generate_audio.py` | Gera SFX e músicas; converte `music.mid` → OGG |

## Regra de dependência

```
app → game → engine
```

`engine` **nunca** conhece `game`. Isso evita ciclos e permite reutilizar o motor
em outros jogos.
