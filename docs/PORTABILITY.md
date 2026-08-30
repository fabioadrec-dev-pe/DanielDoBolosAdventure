# Guia de Portabilidade

O projeto foi escrito pensando em **futuros portes**. Este guia resume como cada
subsistema se traduz para outras plataformas. Cada classe tambem traz notas de
portabilidade nos comentarios (material didatico).

## Principios gerais

- **Isolar o dependente de plataforma.** Apenas `app/DesktopLauncher` conhece a
  janela/SO. `engine` e `game` sao portaveis.
- **Logica em passo fixo (60 Hz).** Casa naturalmente com o VBlank de consoles.
- **Ponto-fixo em vez de float** para CPUs sem FPU (65816/68000): as constantes de
  `GameConfig` (px/s, px/s^2) virariam tabelas 8.8 ou 16.16.
- **Tabelas pre-computadas (LUT)** para seno/cosseno (usados em `Flyer`,
  apresentacao) em hardware sem trigonometria.

## Mapa de equivalencias

| Subsistema (aqui) | SNES (65816) | Mega Drive (68000) | PC/Android/Switch |
|-------------------|--------------|--------------------|-------------------|
| Canvas 256×224 + upscale inteiro (`RenderContext`) | Saida nativa do PPU (256×224) | VDP 320×224 | Render-to-texture + upscale |
| Scroll da camera (`SideScrollerCamera`) | BG1HOFS/BG1VOFS | Regs de scroll do VDP | Matriz de camera |
| Tilemap + solidez (`TileMap`/`TileType`) | Tilemap na VRAM + tabela de atributos | Nametables + tabela | Grade em memoria |
| Colisao por tiles (`TileCollisionResolver`) | Deslocamento >> 4 + leitura do tilemap | Idem | Idem |
| Sprites/animacao (`Assets`) | OBJs (tiles 8×8) + tabela de frames | Sprites do VDP | TextureRegions |
| Input abstrato (`GameInput`) | Registrador do joypad (16 bits) | Leitura do controle | Teclado/gamepad/touch |
| Musica (`AudioManager` streaming) | Sequencia SPC700 | Driver do YM2612/PSG | OGG via OpenAL |
| SFX (`AudioManager` samples) | Amostras BRR (DSP) | PCM/PSG | WAV via OpenAL |
| Paleta/fade (apresentacao) | CGRAM (rotacao/escurecimento) | CRAM | Overlays/shaders |
| Zoom/rotacao (apresentacao) | Modo 7 | Cells escaladas | Batch com rotacao |
| Mosaico (apresentacao) | Registrador MOSAIC | Efeito por software | Shader/soft |

## Efeitos da Tela de Apresentacao (referencia de porte)

- **Fade in/out:** escrever a paleta escurecendo por VBlank (CGRAM/CRAM).
- **Scroll:** registradores de scroll vertical.
- **Zoom/Rotacao:** Modo 7 (SNES); no port PC ja usamos `batch.draw` com escala e
  rotacao.
- **Mudanca de paleta:** rotacao de cores na CGRAM/CRAM.
- **Mosaico:** registrador MOSAIC do SNES; aqui simulado por blocos em software.

## Recomendacoes para um porte SNES (65816)

1. Reescrever `GameConfig` como constantes 8.8 em assembly.
2. Substituir `Animation<TextureRegion>` por tabelas de indices de tile por frame.
3. Fases: exportar `StageFactory`/`StageData` para **dados na ROM** (nao gerar em
   runtime, por custo de CPU) — o formato atual (grade + listas de spawn) mapeia
   diretamente.
4. IA de inimigos: cada `updateAI` vira uma rotina indexada por `enemy_type`.
5. Audio: converter SFX para BRR e a musica para sequencia SPC.

## Recomendacoes para Android

- Novo launcher `AndroidLauncher` (reutiliza `DanielGame`).
- `GameInput.Action` mapeado para botoes virtuais na tela (touch).
- Manter 256×224 virtual e escalar; usar barras (pillarbox) em telas largas.

## Evite

- Chamadas dependentes de SO fora de `app/`.
- Suposicoes de resolucao/aspecto fora de `GameConfig`/`RenderContext`.
- Alocacoes por quadro em caminhos quentes (reaproveite objetos).
