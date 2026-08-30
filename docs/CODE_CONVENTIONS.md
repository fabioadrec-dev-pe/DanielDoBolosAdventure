# Convencoes de Codigo

## Idioma

- **Codigo (identificadores):** ingles ou termos consagrados (ex.: `Player`,
  `TileMap`, `update`, `draw`).
- **Comentarios/documentacao:** portugues, em tom **didatico** (o comentario deve
  ensinar). Sem acentos em alguns arquivos por compatibilidade de encoding no
  build; o conteudo permanece claro.

## Estrutura de pacotes

- Raiz: `com.fabioad.ddba`.
- `engine.*` = motor reutilizavel; **nunca** importa `game.*`.
- `game.*` = jogo; pode importar `engine.*`.
- `app` = launcher (unico dependente de plataforma).

## Comentarios de classe (obrigatorios)

Cada classe abre com um bloco explicando:

1. **Objetivo da classe.**
2. **Funcionamento dos metodos / algoritmos.**
3. **Decisoes de arquitetura.**
4. **Observacoes de portabilidade** (SNES/Mega Drive/PC/Android/Switch).

> Regra pratica: comentarios explicam **por que** e **como (algoritmo)**, nao o
> obvio. Evite comentarios que apenas repetem o codigo.

## Estilo

- Indentacao: 4 espacos, sem tabs.
- Classes utilitarias: `final` + construtor privado.
- Constantes: `static final` em `MAIUSCULAS_COM_UNDERSCORE` (centralizadas em
  `GameConfig` quando globais).
- Campos primeiro, depois construtores, depois metodos publicos, depois privados.
- Preferir imutabilidade quando possivel; evitar estado global (sem singletons).
- Nomes de metodos: verbos (`update`, `draw`, `resolveAxisX`, `handleJump`).

## Principios de design

- **SOLID** quando aplicavel:
  - **S**RP: uma responsabilidade por classe/pacote.
  - **O**CP: novos inimigos = novas subclasses de `Enemy` (sem alterar as demais).
  - **L**SP: subclasses de `Entity`/`Enemy` sao substituiveis.
  - **I**SP: interfaces enxutas (`TileSoliditySource`).
  - **D**IP: engine depende de abstracoes, nao do jogo.
- **DRY:** logica compartilhada em bases (`AbstractScreen`, `Enemy`) e utilitarios
  (`TextUtil`).
- **Performance:** reaproveitar objetos (SpriteBatch unico, `CollisionResult`
  reutilizado), *culling* de tiles, evitar alocacoes por quadro.

## Testes

- Logica pura (colisao, fisica, geracao de fase, sessao) coberta por JUnit 5 em
  `src/test/java`. Nao dependem de OpenGL, rodam em CI headless.

## Commits (sugestao)

- `feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `build:` — mensagens curtas
  focando no **porque**.
