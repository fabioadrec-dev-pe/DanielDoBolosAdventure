#!/usr/bin/env python3
# =============================================================================
# generate_audio.py - Gera SFX e musicas do jogo, e converte music.mid -> OGG.
# -----------------------------------------------------------------------------
# OBJETIVO:
#   1) Sintetizar EFEITOS SONOROS (SFX) do zero em .wav (estilo bipes 8/16 bits).
#   2) Sintetizar MUSICAS simples (menu/chefe/vitoria/game over) e converter p/ OGG.
#   3) Converter automaticamente music.mid -> music/stage.ogg usando timidity+ffmpeg.
#
# DEPENDENCIAS EXTERNAS (ja presentes no ambiente): timidity, ffmpeg.
# Sem dependencias Python externas: usa apenas a stdlib (wave, math, struct).
#
# PORTABILIDADE:
#   - SNES: SFX seriam amostras BRR; musicas, sequencias para o SPC700. Aqui
#     geramos PCM/OGG para o backend OpenAL do desktop.
# =============================================================================

import math
import os
import struct
import subprocess
import wave

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
ASSETS = os.path.join(ROOT, "assets")
SFX_DIR = os.path.join(ASSETS, "sfx")
MUSIC_DIR = os.path.join(ASSETS, "music")
TMP_DIR = os.path.join(os.path.dirname(__file__), "tmp")

SR = 22050  # taxa de amostragem (Hz)

# Frequencias de notas (Hz) usadas nas melodias.
NOTE = {
    "C4": 261.63, "D4": 293.66, "E4": 329.63, "F4": 349.23, "G4": 392.00,
    "A4": 440.00, "B4": 493.88,
    "C5": 523.25, "D5": 587.33, "E5": 659.25, "F5": 698.46, "G5": 783.99,
    "A5": 880.00, "B5": 987.77, "C6": 1046.50,
    "C3": 130.81, "E3": 164.81, "G3": 196.00, "A3": 220.00, "F3": 174.61,
    "R": 0.0,  # pausa
}


def ensure_dirs():
    os.makedirs(SFX_DIR, exist_ok=True)
    os.makedirs(MUSIC_DIR, exist_ok=True)
    os.makedirs(TMP_DIR, exist_ok=True)


# ------------------------- osciladores basicos ------------------------------
def osc(wave_type, freq, t):
    if freq <= 0:
        return 0.0
    phase = (t * freq) % 1.0
    if wave_type == "sine":
        return math.sin(2 * math.pi * phase)
    if wave_type == "square":
        return 1.0 if phase < 0.5 else -1.0
    if wave_type == "saw":
        return 2.0 * phase - 1.0
    if wave_type == "tri":
        return 4.0 * abs(phase - 0.5) - 1.0
    if wave_type == "noise":
        # ruido pseudo-aleatorio deterministico
        x = math.sin((t * 99991.0) % (2 * math.pi)) * 43758.5453
        return (x - math.floor(x)) * 2.0 - 1.0
    return 0.0


def envelope(i, n, attack=0.01, release=0.05):
    """Envelope simples (fade in/out) para evitar cliques."""
    a = int(SR * attack)
    r = int(SR * release)
    if i < a:
        return i / max(1, a)
    if i > n - r:
        return max(0.0, (n - i) / max(1, r))
    return 1.0


def render_notes(seq, wave_type="square", volume=0.5, gap=0.0):
    """Renderiza uma sequencia de (nota, duracao_s) em amostras float."""
    out = []
    for note, dur in seq:
        freq = NOTE.get(note, 0.0)
        n = int(SR * dur)
        for i in range(n):
            t = i / SR
            s = osc(wave_type, freq, t) * volume * envelope(i, n)
            out.append(s)
        for _ in range(int(SR * gap)):
            out.append(0.0)
    return out


def mix(*tracks):
    """Soma varias trilhas (mesmo tamanho ou nao), com clipping suave."""
    n = max(len(t) for t in tracks)
    out = [0.0] * n
    for t in tracks:
        for i, s in enumerate(t):
            out[i] += s
    # normaliza/clipa
    peak = max(1.0, max(abs(s) for s in out) if out else 1.0)
    return [max(-1.0, min(1.0, s / peak)) for s in out]


def write_wav(path, samples):
    with wave.open(path, "w") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(SR)
        frames = bytearray()
        for s in samples:
            frames += struct.pack("<h", int(max(-1.0, min(1.0, s)) * 32000))
        w.writeframes(bytes(frames))


def sweep(f0, f1, dur, wave_type="square", volume=0.5):
    n = int(SR * dur)
    out = []
    phase = 0.0
    for i in range(n):
        t = i / n
        freq = f0 * (1 - t) + f1 * t
        phase += freq / SR
        p = phase % 1.0
        if wave_type == "square":
            s = 1.0 if p < 0.5 else -1.0
        elif wave_type == "saw":
            s = 2 * p - 1
        else:
            s = math.sin(2 * math.pi * p)
        out.append(s * volume * envelope(i, n))
    return out


# ------------------------------- SFX ----------------------------------------
def gen_sfx():
    write_wav(os.path.join(SFX_DIR, "jump.wav"), sweep(300, 760, 0.16, "square", 0.5))
    write_wav(os.path.join(SFX_DIR, "coin.wav"),
              render_notes([("B5", 0.06), ("E6" if "E6" in NOTE else "C6", 0.10)], "square", 0.5))
    write_wav(os.path.join(SFX_DIR, "hurt.wav"), sweep(520, 120, 0.30, "saw", 0.5))
    write_wav(os.path.join(SFX_DIR, "defeat.wav"),
              mix(sweep(400, 90, 0.22, "square", 0.4), render_notes([("R", 0.0)], volume=0)))
    write_wav(os.path.join(SFX_DIR, "break.wav"),
              render_notes([("R", 0.0)]) + [osc("noise", 0, i / SR) * 0.5 * envelope(i, int(SR * 0.15))
                                            for i in range(int(SR * 0.15))])
    write_wav(os.path.join(SFX_DIR, "victory.wav"),
              render_notes([("C5", 0.1), ("E5", 0.1), ("G5", 0.1), ("C6", 0.25)], "square", 0.5))
    write_wav(os.path.join(SFX_DIR, "gameover.wav"),
              render_notes([("G4", 0.2), ("E4", 0.2), ("C4", 0.4)], "tri", 0.5))
    write_wav(os.path.join(SFX_DIR, "menu.wav"), render_notes([("E5", 0.05)], "square", 0.4))
    write_wav(os.path.join(SFX_DIR, "select.wav"), render_notes([("A5", 0.08)], "square", 0.45))
    write_wav(os.path.join(SFX_DIR, "boss.wav"), sweep(140, 60, 0.4, "saw", 0.5))
    print("SFX gerados.")


# ------------------------------- MUSICAS ------------------------------------
def to_ogg(wav_path, ogg_path):
    """Converte WAV -> OGG via ffmpeg (silencioso)."""
    subprocess.run(["ffmpeg", "-y", "-loglevel", "error", "-i", wav_path,
                    "-c:a", "libvorbis", "-qscale:a", "4", ogg_path], check=True)


def loop(seq, times):
    out = []
    for _ in range(times):
        out += seq
    return out


def gen_music():
    # Menu/boss/fase vem dos MIDIs (convert_midi). Aqui so victory/gameover sinteticos.

    # --- VITORIA: fanfarra curta ---
    win = render_notes(loop([("C5", 0.15), ("E5", 0.15), ("G5", 0.15),
                             ("C6", 0.3), ("G5", 0.15), ("C6", 0.5)], 2), "square", 0.45)
    _bounce(win, "victory")

    # --- GAME OVER: descendente melancolico ---
    go = render_notes([("G4", 0.4), ("F4", 0.4), ("E4", 0.4), ("D4", 0.4),
                       ("C4", 0.8)], "tri", 0.45)
    _bounce(go, "gameover")


def _bounce(samples, name):
    wavp = os.path.join(TMP_DIR, name + ".wav")
    write_wav(wavp, samples)
    to_ogg(wavp, os.path.join(MUSIC_DIR, name + ".ogg"))
    print("Musica gerada:", name + ".ogg")


def convert_one_midi(midi_name, out_name):
    """Converte um .mid (timidity PCM -> ffmpeg OGG) com a mesma pipeline do projeto."""
    midi = os.path.join(ROOT, midi_name)
    if not os.path.exists(midi):
        # Tenta tambem na pasta pai (anexos soltos ao lado do projeto).
        alt = os.path.join(os.path.dirname(ROOT), midi_name)
        if os.path.exists(alt):
            midi = alt
        else:
            print("%s nao encontrado; pulando." % midi_name)
            return False
    wavp = os.path.join(TMP_DIR, out_name + ".wav")
    try:
        subprocess.run(["timidity", midi, "-Ow", "-o", wavp], check=True,
                       stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        to_ogg(wavp, os.path.join(MUSIC_DIR, out_name + ".ogg"))
        print("Musica gerada: %s.ogg (a partir de %s)" % (out_name, midi_name))
        return True
    except subprocess.CalledProcessError as e:
        print("Falha na conversao de %s:" % midi_name, e)
        return False


def convert_midi():
    """Converte MIDIs do projeto para OGG (mesmos instrumentos/synth: Timidity)."""
    convert_one_midi("music.mid", "stage")
    convert_one_midi("menu_lady.mid", "menu")
    convert_one_midi("boss.mid", "boss")
    # Tema longo de creditos (apos stage 5). victory.ogg curto das demais fases fica intacto.
    convert_one_midi("victory.mid", "victory_final")


def main():
    ensure_dirs()
    gen_sfx()
    gen_music()
    convert_midi()
    print("Audio finalizado em:", ASSETS)


if __name__ == "__main__":
    main()
