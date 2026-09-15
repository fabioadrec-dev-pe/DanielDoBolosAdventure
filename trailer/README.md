# Daniel do Bolo's Adventure — trailer em tempo real para Android

Este módulo é uma versão nativa da abertura/trailer. Ele não usa MP4: a
`DanielTrailerView` desenha as imagens, textos, zoom, morphing por blocos,
transições pixeladas e fades no `Canvas` a 30 passos por segundo. A `Activity`
sincroniza a animação com `music.ogg`.

## Assets

As imagens foram convertidas para WebP e o MIDI foi renderizado em OGG. O
conjunto de assets fica em aproximadamente 5,6 MB, contra o vídeo final de
aproximadamente 59 MB.

## Integração

Copie `app/src/main/java/com/danieldobolo/adventure/` para o módulo Android do
jogo, copie `app/src/main/assets/` para os assets do mesmo módulo e use:

```java
setContentView(new DanielTrailerView(this));
```

Se precisar da trilha sincronizada, use a `DanielTrailerActivity` como base.
Ela também pausa e retoma o áudio quando o Android suspende a Activity.

## Build deste protótipo

```bash
cd android_rt
gradle assembleDebug
```

O protótipo foi pensado para Android API 23+ e orientação paisagem 16:9.
