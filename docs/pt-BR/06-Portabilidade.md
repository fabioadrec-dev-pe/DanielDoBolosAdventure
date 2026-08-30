# 06 — Guia de Portabilidade

O projeto foi escrito pensando em **futuros portes**. Cada classe importante
também traz notas de portabilidade nos comentários (material didático).

## Princípios gerais

- Isolar o código dependente de plataforma (`DesktopLauncher` é o único ponto).
- Manter a lógica em **passo fixo 60 Hz** — casa naturalmente com o VBlank de consoles.
- Em CPUs sem FPU (65816 / 68000), trocar `float` por **ponto-fixo 8.8 ou 16.16**.
- Usar **tabelas pré-computadas (LUT)** para seno/cosseno em hardware antigo.

## Tabela de equivalências

| Subsistema (Java) | SNES (65816) | Mega Drive (68000) | PC / Android / Switch |
|-------------------|--------------|--------------------|------------------------|
| Canvas 256×224 + upscale (`RenderContext`) | Saída nativa do PPU | VDP 320×224 | Render-to-texture + upscale |
| Câmera (`SideScrollerCamera`) | BG1HOFS / BG1VOFS | Regs de scroll do VDP | Matriz de câmera |
| Tilemap + solidez | Tilemap na VRAM + atributos | Nametables + tabela | Grade em memória |
| Colisão por tiles | Deslocamento >> 4 + leitura do mapa | Idem | Idem |
| Sprites / animação | OBJs (tiles 8×8) + tabela de frames | Sprites do VDP | TextureRegions |
| Input (`GameInput`) | Registrador do joypad (16 bits) | Leitura do controle | Teclado / gamepad / touch |
| Música | Sequência SPC700 | Driver YM2612 / PSG | OGG via OpenAL |
| SFX | Amostras BRR | PCM / PSG | WAV via OpenAL |
| Fade / paleta | CGRAM | CRAM | Overlays / shaders |
| Zoom / rotação | Mode 7 | Cells escaladas | Batch com rotação |
| Mosaico | Registrador MOSAIC | Efeito por software | Shader / software |

## Efeitos da tela de apresentação

| Efeito | No hardware 16 bits |
|--------|---------------------|
| Fade in / out | Escurecer paleta a cada VBlank |
| Scroll | Registradores de scroll vertical |
| Zoom / rotação | Mode 7 (SNES) |
| Mudança de paleta | Rotação de cores na CGRAM / CRAM |
| Mosaico | Registrador MOSAIC (SNES); aqui simulado em software |

## Recomendações para porte SNES (65816)

1. Reescrever `GameConfig` como constantes em ponto-fixo (assembly).
2. Trocar `Animation<TextureRegion>` por tabelas de índices de tile por frame.
3. Exportar `StageData` para **dados na ROM** (não gerar fases em runtime).
4. Cada `updateAI` vira uma rotina indexada por `enemy_type`.
5. Converter SFX para BRR e músicas para sequência SPC.

## Recomendações para Android

1. Criar `AndroidLauncher` (reutiliza `DanielGame`).
2. Mapear `GameInput.Action` para botões virtuais na tela.
3. Manter 256×224 virtual e escalar; usar barras laterais em telas largas.

## Recomendações para Nintendo Switch / PC moderno

- Manter o canvas virtual e o upscale inteiro.
- Substituir apenas o backend de plataforma (LibGDX já cobre desktop; Switch exige
  backend / SDK proprietário).
- Evitar chamadas específicas de SO fora de `app/`.

## O que evitar

- Código dependente de SO espalhado pelo jogo.
- Resoluções “hardcoded” fora de `GameConfig` / `RenderContext`.
- Alocações por quadro em caminhos quentes (reaproveite objetos).
