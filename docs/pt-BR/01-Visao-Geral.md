# 01 — Visão Geral

## Nome do jogo

**Daniel do Bolo's Adventure**

## Objetivo do projeto

Desenvolver um jogo completo de plataforma em **Java**, inspirado na jogabilidade
dos grandes jogos da era **16 bits** (especialmente SNES), com foco em:

- desempenho;
- organização de código;
- facilidade de manutenção;
- preparação para futuros portes (incluindo Super Nintendo / 65816 e Android).

A versão Java é a **implementação principal** e serve de referência para portes futuros.

## Tecnologias

| Tecnologia | Uso |
|------------|-----|
| Java LTS (Eclipse Temurin 17+) | Linguagem e runtime |
| LibGDX | Motor gráfico 2D |
| Gradle | Build e empacotamento |
| OpenGL | Renderização |
| OpenAL | Áudio |
| jlink | Runtime Java mínimo embarcado |
| jpackage | Aplicativos e instaladores nativos |

Todo executável entregue inclui a **JVM embarcada**, dispensando a instalação do
Java pelo usuário final.

## Características do jogo

- Resolução base **256×224** (SNES NTSC), com escalonamento inteiro (pixel-perfect).
- Cinco fases com identidade visual própria, dificuldade crescente, caminhos
  secretos e itens escondidos.
- Física clássica: caminhada, corrida, aceleração, desaceleração, gravidade,
  pulo variável, colisão por tiles e entre entidades.
- Câmera lateral, checkpoints, vidas, moedas, pontuação, tempo, Game Over e vitória.
- Inimigos com IA própria: pequeno, rápido, resistente, voador e chefe.
- Tela inicial animada e tela de apresentação do bairro **Brasília Teimosa**.
- Arte pixel original e áudio gerados por ferramentas do projeto.

## Créditos

- **Desenvolvido por:** fabio_ad
- **Ambientação:** bairro Brasília Teimosa — Recife/PE

## Onde está o código

```
DanielDoBolosAdventure/
├── src/main/java/com/fabioad/ddba/
│   ├── app/       # Launcher desktop
│   ├── engine/    # Motor reutilizável (não depende do jogo)
│   └── game/      # Jogabilidade específica
├── assets/        # Sprites, tiles, música, SFX
├── tools/         # Geradores de assets e áudio
└── docs/          # Documentação (inclui esta pasta pt-BR)
```
