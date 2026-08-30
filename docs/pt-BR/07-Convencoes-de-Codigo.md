# 07 — Convenções de Código

## Idioma

| Contexto | Idioma |
|----------|--------|
| Identificadores (classes, métodos, variáveis) | Inglês ou termos consagrados (`Player`, `update`, `TileMap`) |
| Comentários e documentação | Português (pt-BR), tom **didático** |
| README / docs/pt-BR | Português com acentuação completa |

## Estrutura de pacotes

- Raiz: `com.fabioad.ddba`
- `engine.*` = motor reutilizável — **nunca** importa `game.*`
- `game.*` = jogo — pode importar `engine.*`
- `app` = launcher (único dependente de plataforma)

## Comentários obrigatórios em cada classe

O cabeçalho de cada classe deve explicar:

1. **Objetivo da classe**
2. **Funcionamento dos métodos / algoritmos**
3. **Decisões de arquitetura**
4. **Observações de portabilidade** (SNES, Mega Drive, PC, Android, Switch)

> Comentários explicam o **porquê** e o **como (algoritmo)**, não o óbvio.

## Estilo

- Indentação: 4 espaços (sem tabs).
- Classes utilitárias: `final` + construtor privado.
- Constantes: `static final` em `MAIUSCULAS_COM_UNDERSCORE`.
- Preferir imutabilidade quando fizer sentido.
- Evitar estado global (sem singletons).
- Nomes de métodos: verbos (`update`, `draw`, `handleJump`).

## Princípios SOLID (quando aplicável)

| Princípio | Como aparece no projeto |
|-----------|---------------------------|
| **S**RP | Uma responsabilidade por classe/pacote |
| **O**CP | Novo inimigo = nova subclasse de `Enemy` |
| **L**SP | Subclasses de `Entity`/`Enemy` são substituíveis |
| **I**SP | Interfaces enxutas (`TileSoliditySource`) |
| **D**IP | Engine depende de abstrações, não do jogo |

## DRY e desempenho

- Lógica compartilhada em bases (`AbstractScreen`, `Enemy`) e utilitários (`TextUtil`).
- Reaproveitar objetos (`SpriteBatch` único, `CollisionResult` reutilizado).
- *Culling* de tiles; evitar alocações por quadro.

## Testes

- Lógica pura coberta por JUnit 5 em `src/test/java`.
- Testes não dependem de OpenGL (rodam headless).

## Commits sugeridos

Prefira mensagens curtas focadas no **porquê**:

- `feat:` nova funcionalidade
- `fix:` correção
- `docs:` documentação
- `refactor:` reorganização sem mudar comportamento
- `test:` testes
- `build:` pipeline Gradle / jlink / jpackage
