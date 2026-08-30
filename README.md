# Daniel do Bolo's Adventure

Jogo de plataforma 2D inspirado na era 16 bits (SNES), escrito em **Java** com
**LibGDX**, renderizacao **pixel-perfect** (256×224 com escalonamento inteiro),
fisica de plataforma classica, cinco fases com dificuldade crescente e chefe
final. Empacota a **JVM embarcada** via `jlink`/`jpackage` para rodar sem o
usuario instalar Java.

> Desenvolvido por **fabio_ad**.

---

## Sumario

- [Visao geral](#visao-geral)
- [Como executar (desenvolvimento)](#como-executar-desenvolvimento)
- [Controles](#controles)
- [Como gerar os executaveis (JVM embarcada)](#como-gerar-os-executaveis-jvm-embarcada)
- [Regerar assets e audio](#regerar-assets-e-audio)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Documentacao (pt-BR)](#documentacao-pt-br)
- [Requisitos](#requisitos)

---

## Visao geral

- **Resolucao base:** 256×224 (SNES NTSC), desenhada num framebuffer e ampliada
  por fator **inteiro** (pixel-perfect, com letterbox).
- **Fisica:** passo fixo (60 Hz) deterministico; aceleracao/atrito, gravidade,
  **pulo variavel**, colisao por tiles (eixo separado) e entre entidades.
- **Fluxo de telas:** Boot → Titulo (animado) → Apresentacao (ruas de Brasilia
  Teimosa, com fade/scroll/zoom/rotacao/paleta/mosaico) → Menu → Fases → Vitoria
  ou Game Over.
- **Inimigos com IA propria:** `Walker`, `FastRunner`, `Tank`, `Flyer`, `Boss`.
- **HUD:** vidas, moedas, pontuacao, tempo e fase.
- **Audio:** musica (menu/fase/chefe/vitoria/game over) e 10 SFX; `music.mid` e
  convertido automaticamente para OGG.

Arquitetura **modular** com separacao estrita: o pacote `engine` **nunca** depende
de `game` (regra que evita dependencias circulares). Veja
[`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md).

---

## Como executar (desenvolvimento)

```bash
./gradlew run
```

Isso compila e abre o jogo numa janela 768×672 (3×). Requer um ambiente grafico
com OpenGL.

---

## Controles

| Acao        | Teclas                     |
|-------------|----------------------------|
| Mover       | Setas ou `A` / `D`         |
| Olhar/Abaixar | `W` / `S` ou Cima/Baixo   |
| Correr      | `Shift` ou `X`             |
| Pular       | `Espaco` ou `Z`            |
| Confirmar / Pausar | `Enter`             |
| Voltar / Menu | `Esc`                    |

O mapeamento e abstrato (`engine.input.GameInput`), pronto para gamepad ou o
controle do SNES em um porte.

---

## Como gerar os executaveis (JVM embarcada)

O empacotamento usa `jlink` (runtime minimo) + `jpackage` (app nativo). Cada SO
gera o seu formato (nao ha cross-build):

```bash
# Aplicativo nativo portatil (pasta executavel) com JVM embarcada:
./gradlew packageApp

# Instalador nativo do SO atual (msi no Windows, deb no Linux, dmg no macOS):
./gradlew installer
```

Resultado (Linux gera `Daniel do Bolo's Adventure`, Windows `.exe`, macOS `.app`):

- app-image em `~/DanielDoBolosAdventure-build/dist/`
- instalador em `~/DanielDoBolosAdventure-build/installer/`

> **Por que fora de `build/`?** O `jlink` cria *hard links* nos arquivos de
> licenca do runtime, o que **falha em NTFS/exFAT** (particoes Windows montadas
> no Linux, HDs externos). Por isso o destino padrao e o `HOME` do usuario.
> Para escolher outro destino: `./gradlew packageApp -PdistDir=/caminho`.

Detalhes completos em [`docs/BUILD.md`](docs/BUILD.md).

---

## Regerar assets e audio

Toda a arte e o audio sao **gerados por ferramentas** (nada de recursos de
terceiros). Para recria-los:

```bash
# Arte pixel (sprites, tiles, fundos) via Python + Pillow:
python3 tools/generate_assets.py

# SFX + musicas + conversao de music.mid -> OGG (requer ffmpeg e timidity):
python3 tools/generate_audio.py
```

---

## Estrutura do projeto

```
DanielDoBolosAdventure/
├── build.gradle / settings.gradle / gradle.properties   # build + jlink/jpackage
├── assets/            # recursos em runtime (sprites, tiles, textures, music, sfx, ui...)
├── docs/              # documentacao (arquitetura, portabilidade, build, ...)
├── tools/             # geradores de assets e audio
├── music.mid          # trilha-fonte convertida para OGG
└── src/
    ├── main/java/com/fabioad/ddba/
    │   ├── app/       # DesktopLauncher (main; modulos "app"+"launcher")
    │   ├── engine/    # motor reutilizavel (core, renderer, camera, input,
    │   │              #   collision, audio, assets, ui) — NAO depende de game
    │   └── game/      # jogo (core, stages, player, enemies, entities, maps,
    │                  #   tiles, hud, data) — depende de engine
    └── test/java/...  # testes de logica pura (colisao, fisica, fases, sessao)
```

O mapeamento entre os "modulos" pedidos e os pacotes Java esta em
[`docs/MODULES.md`](docs/MODULES.md).

---

## Documentacao (pt-BR)

Documentacao completa em **portugues brasileiro** (com acentuacao):

**[docs/pt-BR/README.md](docs/pt-BR/README.md)**

| Documento | Conteudo |
|-----------|----------|
| [01-Visao-Geral.md](docs/pt-BR/01-Visao-Geral.md) | Visao geral, objetivos e stack |
| [02-Manual-de-Compilacao.md](docs/pt-BR/02-Manual-de-Compilacao.md) | Como compilar, executar e gerar instaladores |
| [03-Arquitetura.md](docs/pt-BR/03-Arquitetura.md) | Arquitetura + fluxogramas da engine e do gameplay |
| [04-Modulos.md](docs/pt-BR/04-Modulos.md) | Explicacao de cada modulo |
| [05-Diagrama-de-Classes.md](docs/pt-BR/05-Diagrama-de-Classes.md) | Diagrama de classes |
| [06-Portabilidade.md](docs/pt-BR/06-Portabilidade.md) | Guia de portabilidade |
| [07-Convencoes-de-Codigo.md](docs/pt-BR/07-Convencoes-de-Codigo.md) | Convencoes de codigo |
| [08-Estrutura-dos-Assets.md](docs/pt-BR/08-Estrutura-dos-Assets.md) | Estrutura e pipeline dos assets |
| [09-Pipeline-de-Build.md](docs/pt-BR/09-Pipeline-de-Build.md) | Pipeline de build (Gradle / jlink / jpackage) |
| [10-Controles-e-Jogabilidade.md](docs/pt-BR/10-Controles-e-Jogabilidade.md) | Controles e mecanicas |

Documentacao tecnica complementar (tambem em portugues) em `docs/`:

| Documento | Conteudo |
|-----------|----------|
| [ARCHITECTURE.md](docs/ARCHITECTURE.md) | Arquitetura + fluxogramas |
| [MODULES.md](docs/MODULES.md) | Modulos |
| [CLASS_DIAGRAM.md](docs/CLASS_DIAGRAM.md) | Diagrama de classes |
| [BUILD.md](docs/BUILD.md) | Compilacao e build |
| [PORTABILITY.md](docs/PORTABILITY.md) | Portabilidade |
| [CODE_CONVENTIONS.md](docs/CODE_CONVENTIONS.md) | Convencoes |
| [ASSETS.md](docs/ASSETS.md) | Assets |

---

## Requisitos

- **JDK 17+** (recomendado Eclipse Temurin 21/25 quando consolidado no ambiente).
- **Gradle wrapper** incluso (`./gradlew`), nao precisa instalar Gradle.
- Para regerar assets/audio: **Python 3 + Pillow**, **ffmpeg** e **timidity**.

## Licenca / creditos

Codigo, arte, musica e efeitos originais, criados para este projeto.
Ambientacao inspirada no bairro **Brasilia Teimosa** (Recife/PE).
