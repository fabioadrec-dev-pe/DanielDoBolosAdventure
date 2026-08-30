package com.fabioad.ddba.engine.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * AudioManager
 * =============================================================================
 * OBJETIVO DA CLASSE:
 *   Gerenciar toda a AUDIO do jogo: musicas (streaming, uma por vez) e efeitos
 *   sonoros (SFX curtos, carregados na memoria). Centraliza volumes e evita que
 *   duas musicas toquem simultaneamente.
 *
 * DIFERENCA MUSIC x SOUND (LibGDX):
 *   - Music: fluxo (streaming) de arquivos maiores (musica de fundo). Loop nativo.
 *   - Sound: carregado inteiro na memoria; ideal para SFX curtos e de baixa latencia.
 *
 * DECISOES DE ARQUITETURA:
 *   - Carregamento tardio e cache em mapas (nome -> recurso).
 *   - Uma unica referencia "currentMusic" garante troca limpa de trilha.
 *
 * PORTABILIDADE:
 *   - SNES: audio e gerado pelo chip SPC700 + DSP a partir de amostras BRR e uma
 *     sequencia (tracker). "Music" viraria uma sequencia SPC; "Sound" viraria
 *     amostras BRR disparadas por voz. Este manager e o analogo de alto nivel do
 *     "driver de som".
 *   - PC/Mobile/Switch: o backend (OpenAL) ja abstrai o hardware.
 */
public final class AudioManager implements Disposable {

    private final ObjectMap<String, Sound> sounds = new ObjectMap<>();
    private Music currentMusic;
    private String currentMusicPath;

    private float musicVolume = 0.7f;
    private float sfxVolume = 0.9f;

    /**
     * Toca uma musica em loop (streaming). Se a mesma ja estiver tocando, ignora.
     *
     * @param internalPath caminho relativo em assets (ex.: "music/menu.ogg")
     * @param loop         repetir ao terminar
     */
    public void playMusic(String internalPath, boolean loop) {
        if (internalPath.equals(currentMusicPath) && currentMusic != null && currentMusic.isPlaying()) {
            return;
        }
        stopMusic();
        if (!Gdx.files.internal(internalPath).exists()) {
            Gdx.app.log("AudioManager", "Musica ausente (ignorada): " + internalPath);
            return;
        }
        currentMusic = Gdx.audio.newMusic(Gdx.files.internal(internalPath));
        currentMusic.setLooping(loop);
        currentMusic.setVolume(musicVolume);
        currentMusic.play();
        currentMusicPath = internalPath;
    }

    /** Para e libera a musica atual. */
    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic.dispose();
            currentMusic = null;
            currentMusicPath = null;
        }
    }

    /** Pre-carrega um SFX em memoria sob um nome logico. */
    public void loadSfx(String name, String internalPath) {
        if (sounds.containsKey(name)) return;
        if (!Gdx.files.internal(internalPath).exists()) {
            Gdx.app.log("AudioManager", "SFX ausente (ignorado): " + internalPath);
            return;
        }
        sounds.put(name, Gdx.audio.newSound(Gdx.files.internal(internalPath)));
    }

    /** Toca um SFX previamente carregado (no-op se ausente). */
    public void playSfx(String name) {
        Sound s = sounds.get(name);
        if (s != null) {
            s.play(sfxVolume);
        }
    }

    public void setMusicVolume(float v) {
        this.musicVolume = clamp01(v);
        if (currentMusic != null) currentMusic.setVolume(this.musicVolume);
    }

    public void setSfxVolume(float v) {
        this.sfxVolume = clamp01(v);
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public float getSfxVolume() {
        return sfxVolume;
    }

    private static float clamp01(float v) {
        return v < 0 ? 0 : (v > 1 ? 1 : v);
    }

    @Override
    public void dispose() {
        stopMusic();
        for (Sound s : sounds.values()) {
            s.dispose();
        }
        sounds.clear();
    }
}
