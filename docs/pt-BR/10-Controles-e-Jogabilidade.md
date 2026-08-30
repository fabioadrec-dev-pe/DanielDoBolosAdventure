# 10 — Controles e Jogabilidade

## Controles

| Ação | Teclas |
|------|--------|
| Mover | Setas ou `A` / `D` |
| Olhar para cima | Seta para cima ou `W` |
| Abaixar | Seta para baixo ou `S` |
| Correr | `Shift` ou `X` |
| Pular | `Espaço` ou `Z` |
| Confirmar / Pausar | `Enter` |
| Voltar / Menu | `Esc` |

O mapeamento é abstrato (`GameInput`) e já está preparado para gamepad ou o
controle do SNES em um porte futuro.

## Fluxo do jogo

1. **Tela inicial** — título animado + crédito “Developed by fabio_ad”
2. **Apresentação** — fundo preto; ruas de Brasília Teimosa com fade, scroll,
   zoom, rotação, mudança de paleta e mosaico. `ENTER` volta ao menu
3. **Menu** — Novo Jogo / Sair
4. **Fases 1–5** — progresso, checkpoints, inimigos, moedas, objetivo
5. **Vitória** — após a 5ª fase: créditos rolando
6. **Game Over** — sem vidas: pontuação final e retorno ao menu

## Mecânicas

| Mecânica | Descrição |
|----------|-----------|
| Caminhada / corrida | Aceleração e desaceleração progressivas |
| Gravidade | Puxa o herói para baixo; queda limitada |
| Pulo variável | Segurar o botão = pulo mais alto |
| Colisão por tiles | Chão, paredes, teto, espinhos |
| Colisão entre entidades | Pisão derrota inimigos; toque lateral causa dano |
| Câmera lateral | Segue o jogador com suavização |
| Checkpoints | Respawn no último ponto alcançado |
| Vidas | Começa com 3; 100 moedas = vida extra |
| Moedas / pontuação | Coleta e derrota de inimigos somam pontos |
| Tempo | Cada fase tem tempo limite |
| Game Over / Vitória | Telas de fim de jogo |

## HUD

Exibe em tempo real:

- vidas
- moedas
- pontuação
- tempo restante
- número da fase

## Fases

| Fase | Tema | Destaque |
|------|------|----------|
| 1 | Orla de Brasília Teimosa | Introdução; inimigos básicos |
| 2 | Ruas do bairro | Flyer entra no pool |
| 3 | Cais e jangadas | Espinhos nos buracos; inimigo rápido |
| 4 | Ferro-velho | Tank (resistente) |
| 5 | Castelo do chefe | Boss final |

Cada fase tem identidade visual própria, obstáculos, dificuldade crescente,
caminhos secretos e itens escondidos.

## Inimigos

| Tipo | Nome interno | IA |
|------|--------------|-----|
| Pequeno | `Walker` | Patrulha lenta; 1 pisão |
| Rápido | `FastRunner` | Patrulha rápida; 1 pisão |
| Resistente | `Tank` | Lento; 3 pisões |
| Voador | `Flyer` | Onda senoidal no ar; 1 pisão |
| Chefe | `Boss` | Patrulha + saltos; 5 pisões |

## Dicas rápidas

- Segure o pulo para alcançar plataformas altas.
- Pise nos inimigos **caindo** (pés acima do centro) — não de lado.
- Ative os checkpoints; em morte você volta ao último.
- A fase 5 tem música e oposição diferente (chefe).
