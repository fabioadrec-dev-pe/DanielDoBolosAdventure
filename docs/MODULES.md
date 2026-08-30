# Explicacao dos Modulos

O enunciado lista varios "modulos" (core, graphics, renderer, animation, camera,
input, physics, collision, audio, music, sfx, maps, tiles, sprites, entities,
enemies, player, hud, ui, menus, save, stages, particles, shaders, assets, tools).
Neste projeto eles sao **pacotes Java** organizados sob `engine.*` e `game.*`,
com dependencia sempre no sentido `game → engine`.

> **Decisao:** modulo Gradle unico com pacotes bem separados. Mantem o build
> simples e reproduzivel e evita dependencias circulares por convencao. Cada
> pacote pode virar um sub-projeto Gradle depois, sem reescrever codigo.

## Engine (`com.fabioad.ddba.engine`)

| Pacote | Responsabilidade | Classes principais |
|--------|------------------|--------------------|
| `core` | Loop, telas, contexto, constantes | `GameConfig`, `GameContext`, `AbstractScreen` |
| `renderer` (graphics/renderer) | Pipeline pixel-perfect (FBO + upscale inteiro) | `RenderContext` |
| `camera` | Camera lateral com follow e clamp | `SideScrollerCamera` |
| `input` | Acoes logicas abstraidas do hardware | `GameInput` |
| `collision` (physics/collision) | AABB + resolucao contra tiles | `AABB`, `TileCollisionResolver`, `TileSoliditySource` |
| `audio` (music/sfx) | Musica (streaming) e SFX (memoria) | `AudioManager` |
| `assets` | Carregar/fatiar texturas e montar animacoes | `Assets`, `AssetPaths` |
| `ui` | Utilitarios de texto (menus, titulo, creditos) | `TextUtil` |

Observacoes:
- **animation**: representada por `com.badlogic.gdx.graphics.g2d.Animation`, montada
  em `Assets` (fatiamento de spritesheets).
- **physics**: as constantes vivem em `GameConfig`; a integracao (gravidade,
  aceleracao) e feita nas entidades; a interacao com o mundo e a `collision`.
- **particles/shaders**: pastas de assets reservadas; ganchos previstos no engine
  (ver TODOs) para expansao futura.

## Game (`com.fabioad.ddba.game`)

| Pacote | Responsabilidade | Classes principais |
|--------|------------------|--------------------|
| `core` | Ponto central, sessao e base de telas | `DanielGame`, `GameSession`, `BaseGameScreen` |
| `stages` (menus/stages) | Todas as telas do jogo | `BootScreen`, `TitleScreen`, `PresentationScreen`, `MenuScreen`, `PlayScreen`, `GameOverScreen`, `VictoryScreen` |
| `player` | Heroi: fisica, estados, animacao | `Player`, `PlayerState` |
| `enemies` | IA por tipo | `Enemy`, `Walker`, `FastRunner`, `Tank`, `Flyer`, `Boss`, `EnemyFactory` |
| `entities` | Base de entidades e itens | `Entity`, `Coin` |
| `maps` | Fases (dados + geracao) | `TileMap`, `StageData`, `StageFactory` |
| `tiles` | Tipos e propriedades de tile | `TileType` |
| `hud` | Placar na tela | `Hud` |
| `data` | Dados de ambientacao | `BrasiliaTeimosaStreets` |
| `save` | Estado da partida em memoria | `GameSession` (persistencia em disco = TODO) |

## App / Launcher

| Pacote | Responsabilidade | Classe |
|--------|------------------|--------|
| `app` | Entrada desktop (janela LWJGL3) | `DesktopLauncher` |

E a **unica** classe dependente de plataforma. Novos launchers (Android/HTML)
reutilizam `DanielGame` sem alteracao.

## Tools

Scripts fora do codigo Java, em `tools/`:

- `generate_assets.py` — gera sprites, tiles e fundos (Pillow).
- `generate_audio.py` — gera SFX e musicas e converte `music.mid` → OGG.
