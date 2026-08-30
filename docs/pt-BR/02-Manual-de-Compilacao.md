# 02 — Manual de Compilação

## Requisitos

- **JDK 17 ou superior** (recomendado Eclipse Temurin 21/25 quando disponível).
- **Gradle Wrapper** incluso no projeto (`./gradlew`) — não é necessário instalar o Gradle.
- Acesso à internet na **primeira** compilação (baixa LibGDX e dependências).
- Ambiente gráfico com OpenGL para jogar (`./gradlew run`).

Para regenerar assets/áudio:

- Python 3 + Pillow
- `ffmpeg`
- `timidity`

## Executar em desenvolvimento

```bash
cd DanielDoBolosAdventure
./gradlew run
```

A janela abre em **768×672** (3× a resolução 256×224).

## Compilar e testar

```bash
./gradlew compileJava   # só compila
./gradlew test          # testes de lógica (colisão, fases, sessão)
./gradlew build         # compilação + testes + jars
```

## Gerar o fat jar

```bash
./gradlew shadowJar
```

Saída: `build/libs/DanielDoBolosAdventure-<versão>-all.jar`

## Gerar aplicativo com JVM embarcada

```bash
./gradlew packageApp
```

Gera um aplicativo nativo **completo**, com a JVM embutida via `jlink` + `jpackage`.

| Sistema operacional | Resultado típico |
|---------------------|------------------|
| Linux | pasta com o binário `Daniel do Bolo's Adventure` |
| Windows | pasta com `Daniel do Bolo's Adventure.exe` |
| macOS | `Daniel do Bolo's Adventure.app` |

## Gerar instalador

```bash
./gradlew installer
```

| Sistema | Instalador |
|---------|------------|
| Windows | `.msi` |
| Linux | `.deb` |
| macOS | `.dmg` |

> **Importante:** `jpackage` só gera artefatos para o **sistema em que está
> rodando** (não há cross-build). Para Windows, Linux e macOS, execute o build
> em cada plataforma.

## Onde ficam os artefatos

Por padrão:

```
~/DanielDoBolosAdventure-build/
├── dist/        # aplicativo nativo (app-image)
├── installer/   # instalador nativo
└── runtime/     # runtime Java gerado pelo jlink
```

Motivo: o `jlink` cria *hard links* e **falha em NTFS/exFAT** (HDs externos /
partições Windows montadas no Linux). O destino padrão é o `HOME` do usuário.

Para escolher outro destino:

```bash
./gradlew packageApp -PdistDir=/caminho/desejado
```

## Regenerar arte e áudio

```bash
python3 tools/generate_assets.py   # sprites, tiles, fundos
python3 tools/generate_audio.py    # SFX + músicas + conversão music.mid → OGG
```

## Problemas comuns

| Problema | Solução |
|----------|---------|
| `jlink` falha com “Operação não permitida” | Use `-PdistDir` em um FS nativo (ext4/APFS) |
| Jogo não abre | Verifique OpenGL / drivers gráficos |
| Dependências não baixam | Confira internet e Maven Central |
| Áudio sem MIDI | Instale `timidity` e `ffmpeg`, rode `generate_audio.py` |
