#!/usr/bin/env python3
# =============================================================================
# generate_assets.py - Gerador de arte pixel (placeholder) para o jogo.
# -----------------------------------------------------------------------------
# OBJETIVO:
#   Criar, do zero e programaticamente, todos os PNGs de sprites, tiles e fundos
#   com estetica 16 bits, ja no LAYOUT exato esperado por Assets.java (larguras
#   de quadro fixas em faixa horizontal). Assim o jogo roda "de fabrica".
#
# DECISAO:
#   Arte gerada por codigo (deterministica) serve como PLACEHOLDER original e
#   evita depender de arquivos externos. Substituir depois por arte manual e
#   trivial: basta manter as MESMAS dimensoes de quadro.
#
# LAYOUT (deve casar com Assets.java):
#   daniel.png      18 quadros 24x32   (idle,walk,run,jump,fall,crouch,lookup,hurt,dead,victory)
#   coin.png         4 quadros 16x16
#   enemy_walker.png 4 quadros 24x24   | enemy_flyer.png 2x16 | enemy_fast.png 4x16
#   enemy_tank.png   2 quadros 24x24   | boss.png 2 quadros 48x48
#   tileset.png      8 tiles 16x16 (id 0 = transparente)
#   bg_stageN.png    256x224 (um por fase)
# =============================================================================

import math
import os
from PIL import Image, ImageDraw

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
ASSETS = os.path.join(ROOT, "assets")


def ensure_dirs():
    for d in ["sprites", "tiles", "textures", "music", "sfx", "fonts", "maps", "shaders", "ui"]:
        os.makedirs(os.path.join(ASSETS, d), exist_ok=True)


def new_sheet(frames, fw, fh):
    """Cria uma folha RGBA transparente com 'frames' quadros de fw x fh."""
    img = Image.new("RGBA", (fw * frames, fh), (0, 0, 0, 0))
    return img


def px(draw, x, y, color):
    draw.point((x, y), fill=color)


# --------------------------- PERSONAGEM: DANIEL ------------------------------
# Paleta do heroi (tema "senhor da praia"): idoso careca/grisalho, sem camisa,
# barrigudo, de bermuda e com uma garrafa de cerveja na mao (ver imagem-ref).
SKIN = (232, 186, 148, 255)        # pele
SKIN_SHADE = (200, 152, 116, 255)  # sombra da barriga
HAIR = (206, 206, 210, 255)        # cabelo grisalho (laterais)
CHEST_HAIR = (120, 92, 68, 255)    # pelos do peito
SHORTS = (60, 82, 150, 255)        # bermuda azul
SHORTS_SH = (44, 60, 118, 255)     # sombra da bermuda
BOTTLE = (96, 52, 22, 255)         # garrafa de cerveja (ambar escuro)
BOTTLE_HI = (156, 100, 44, 255)    # brilho da garrafa
LABEL = (240, 236, 208, 255)       # rotulo claro
LABEL_ART = (92, 168, 92, 255)     # detalhe verde do rotulo
SHOE = (78, 60, 44, 255)           # chinelo/sandalia
OUTLINE = (25, 20, 30, 255)
EYE = (30, 30, 40, 255)


def _bottle(d, x, y):
    """Desenha uma GARRAFA DE CERVEJA (marca do personagem).
    (x, y) = canto superior-esquerdo do CORPO da garrafa (4 de largura x 8 de altura).
    Composta por: corpo ambar, gargalo, tampinha, rotulo e um brilho lateral."""
    d.rectangle([x, y, x + 3, y + 7], fill=BOTTLE, outline=OUTLINE)   # corpo
    d.rectangle([x + 1, y - 3, x + 2, y], fill=BOTTLE)               # gargalo
    px(d, x + 1, y - 4, (55, 55, 65, 255))                          # tampa
    px(d, x + 2, y - 4, (55, 55, 65, 255))
    d.rectangle([x, y + 3, x + 3, y + 6], fill=LABEL)               # rotulo
    px(d, x + 1, y + 4, LABEL_ART)                                  # arte do rotulo
    px(d, x + 2, y + 5, LABEL_ART)
    px(d, x, y + 1, BOTTLE_HI)                                       # brilho


def draw_daniel_frame(img, fi, fw, fh, pose, leg=0, arm=0):
    """Desenha um quadro do heroi 'Daniel': senhor idoso, careca e grisalho, SEM
    CAMISA, barrigudo (com pelos no peito), de BERMUDA e segurando uma GARRAFA
    DE CERVEJA — inspirado na imagem de referencia. Mantem o quadro 24x32 e os
    parametros leg/arm para animar (troca de pernas/bracos)."""
    d = ImageDraw.Draw(img)
    ox = fi * fw
    cx = ox + fw // 2

    # ---------- CABECA: topo careca, cabelo grisalho nas laterais ----------
    head_y = 3
    d.rectangle([cx - 5, head_y + 4, cx + 4, head_y + 9], fill=HAIR)               # coroa grisalha
    d.rectangle([cx - 4, head_y, cx + 3, head_y + 8], fill=SKIN, outline=OUTLINE)  # rosto/careca
    px(d, cx - 5, head_y + 5, SKIN)                                                # orelhas
    px(d, cx + 4, head_y + 5, SKIN)
    px(d, cx - 2, head_y + 3, HAIR)                                                # sobrancelhas
    px(d, cx + 1, head_y + 3, HAIR)
    look = -1 if pose == "lookup" else 0
    px(d, cx - 2, head_y + 4 + look, EYE)                                          # olhos
    px(d, cx + 1, head_y + 4 + look, EYE)
    px(d, cx, head_y + 5, SKIN_SHADE)                                             # nariz
    d.rectangle([cx - 1, head_y + 6, cx + 1, head_y + 7], fill=(120, 60, 60, 255)) # boca (falando)
    if pose == "dead":
        px(d, cx - 2, head_y + 4, (200, 30, 30, 255))                            # olhos em X
        px(d, cx + 1, head_y + 4, (200, 30, 30, 255))

    # ---------- TRONCO NU + BARRIGA REDONDA ----------
    chest_top = head_y + 8            # ~11
    d.rectangle([cx - 6, chest_top, cx + 5, chest_top + 4], fill=SKIN, outline=OUTLINE)  # peito/ombros
    belly_top = chest_top + 3         # ~14
    d.ellipse([cx - 7, belly_top, cx + 6, belly_top + 12], fill=SKIN, outline=OUTLINE)   # barriga
    d.ellipse([cx - 4, belly_top + 6, cx + 5, belly_top + 12], fill=SKIN_SHADE)          # sombra baixa
    px(d, cx, belly_top + 8, SKIN_SHADE)                                                 # umbigo
    # pelos do peito (mancha central) + mamilos, por cima da pele
    for hx, hy in [(-2, 1), (1, 1), (-1, 2), (0, 3), (2, 2), (-3, 2), (2, 3)]:
        px(d, cx + hx, chest_top + hy, CHEST_HAIR)
    px(d, cx - 3, chest_top + 2, (150, 100, 90, 255))
    px(d, cx + 3, chest_top + 2, (150, 100, 90, 255))

    # ---------- BERMUDA ----------
    shorts_top = belly_top + 9        # ~23
    d.rectangle([cx - 6, shorts_top, cx + 5, shorts_top + 5], fill=SHORTS, outline=OUTLINE)
    px(d, cx, shorts_top + 3, SHORTS_SH)                                          # vinco central
    px(d, cx, shorts_top + 4, SHORTS_SH)

    # ---------- PERNAS NUAS (animadas) + CHINELOS ----------
    leg_top = shorts_top + 5          # ~28
    if pose in ("jump", "crouch"):
        # pernas dobradas (pulo/agachado)
        d.rectangle([cx - 5, leg_top - 1, cx - 1, leg_top + 2], fill=SKIN, outline=OUTLINE)
        d.rectangle([cx + 1, leg_top - 2, cx + 5, leg_top + 1], fill=SKIN, outline=OUTLINE)
        d.rectangle([cx - 6, leg_top + 2, cx - 1, leg_top + 3], fill=SHOE)
        d.rectangle([cx + 1, leg_top + 1, cx + 6, leg_top + 2], fill=SHOE)
    else:
        lyl = min(31, leg_top + 3 + leg)
        lyr = min(31, leg_top + 3 - leg)
        d.rectangle([cx - 5, leg_top, cx - 1, lyl], fill=SKIN, outline=OUTLINE)
        d.rectangle([cx + 1, leg_top, cx + 5, lyr], fill=SKIN, outline=OUTLINE)
        d.rectangle([cx - 6, lyl, cx - 1, lyl], fill=SHOE)                        # chinelo esq
        d.rectangle([cx + 1, lyr, cx + 6, lyr], fill=SHOE)                        # chinelo dir

    # ---------- BRACO DIREITO (sem garrafa) ----------
    if pose == "victory":
        d.rectangle([cx + 5, head_y + 3, cx + 7, chest_top + 2], fill=SKIN, outline=OUTLINE)
    else:
        ay = chest_top + 1 + max(0, arm)
        d.rectangle([cx + 5, ay, cx + 7, min(31, ay + 6)], fill=SKIN, outline=OUTLINE)

    # ---------- BRACO ESQUERDO + GARRAFA DE CERVEJA (marca do personagem) ----------
    if pose == "victory":
        # ergue a garrafa comemorando
        d.rectangle([cx - 7, head_y + 2, cx - 5, chest_top + 2], fill=SKIN, outline=OUTLINE)
        _bottle(d, cx - 8, head_y - 1)
    else:
        # antebraco dobrado trazendo a garrafa a frente do peito
        d.rectangle([cx - 8, chest_top + 1, cx - 6, chest_top + 5], fill=SKIN, outline=OUTLINE)
        d.rectangle([cx - 8, chest_top + 4, cx - 5, chest_top + 5], fill=SKIN)  # mao segurando
        _bottle(d, cx - 11, chest_top)


def gen_daniel():
    fw, fh = 24, 32
    frames = 18
    img = new_sheet(frames, fw, fh)
    # 0-1 idle
    draw_daniel_frame(img, 0, fw, fh, "idle", leg=0, arm=0)
    draw_daniel_frame(img, 1, fw, fh, "idle", leg=0, arm=1)
    # 2-5 walk
    for i, l in enumerate([1, 0, -1, 0]):
        draw_daniel_frame(img, 2 + i, fw, fh, "walk", leg=l, arm=l)
    # 6-9 run (passos maiores)
    for i, l in enumerate([2, 0, -2, 0]):
        draw_daniel_frame(img, 6 + i, fw, fh, "run", leg=l, arm=l)
    # 10 jump, 11 fall
    draw_daniel_frame(img, 10, fw, fh, "jump")
    draw_daniel_frame(img, 11, fw, fh, "jump", leg=1)
    # 12 crouch
    draw_daniel_frame(img, 12, fw, fh, "crouch")
    # 13 lookup
    draw_daniel_frame(img, 13, fw, fh, "lookup")
    # 14 hurt
    draw_daniel_frame(img, 14, fw, fh, "walk", leg=-1, arm=2)
    # 15 dead
    draw_daniel_frame(img, 15, fw, fh, "dead")
    # 16-17 victory
    draw_daniel_frame(img, 16, fw, fh, "victory", arm=0)
    draw_daniel_frame(img, 17, fw, fh, "victory", arm=1)
    img.save(os.path.join(ASSETS, "sprites", "daniel.png"))


# ------------------------------- PEIXE (colecionavel, era "moeda") ------------
def gen_coin():
    """Gera um PEIXE girando (4 frames 16x16). Mantem o arquivo coin.png e o
    layout esperado por Assets.java — so troca a arte da moeda por peixe."""
    fw = fh = 16
    img = new_sheet(4, fw, fh)
    d = ImageDraw.Draw(img)

    # Paleta peixe (estilo jangada / Brasilia Teimosa)
    BODY = (255, 140, 50, 255)       # laranja
    BODY_D = (210, 90, 30, 255)      # sombra
    BELLY = (255, 210, 140, 255)     # barriga clara
    FIN = (230, 70, 50, 255)         # nadadeira vermelha
    EYE_W = (255, 255, 255, 255)
    EYE_B = (30, 30, 40, 255)

    def fish_side(ox, flip=False):
        """Peixe de perfil (vista lateral). flip=True espelha horizontalmente."""
        # corpo oval
        if not flip:
            d.ellipse([ox + 2, 4, ox + 12, 12], fill=BODY, outline=OUTLINE)
            d.ellipse([ox + 4, 7, ox + 11, 12], fill=BELLY)  # barriga
            # cabeca / focinho
            d.polygon([(ox + 12, 8), (ox + 15, 6), (ox + 15, 10)], fill=BODY, outline=OUTLINE)
            # cauda
            d.polygon([(ox + 2, 8), (ox - 1, 4), (ox - 1, 12)], fill=FIN, outline=OUTLINE)
            # nadadeira dorsal
            d.polygon([(ox + 6, 4), (ox + 8, 1), (ox + 10, 4)], fill=FIN, outline=OUTLINE)
            # olho
            px(d, ox + 11, 7, EYE_W)
            px(d, ox + 11, 7, EYE_B)
            # brilho no corpo
            px(d, ox + 6, 6, BELLY)
        else:
            d.ellipse([ox + 3, 4, ox + 13, 12], fill=BODY, outline=OUTLINE)
            d.ellipse([ox + 4, 7, ox + 11, 12], fill=BELLY)
            d.polygon([(ox + 3, 8), (ox + 0, 6), (ox + 0, 10)], fill=BODY, outline=OUTLINE)
            d.polygon([(ox + 13, 8), (ox + 16, 4), (ox + 16, 12)], fill=FIN, outline=OUTLINE)
            d.polygon([(ox + 5, 4), (ox + 7, 1), (ox + 9, 4)], fill=FIN, outline=OUTLINE)
            px(d, ox + 4, 7, EYE_W)
            px(d, ox + 4, 7, EYE_B)
            px(d, ox + 9, 6, BELLY)

    def fish_three_quarter(ox, facing_right=True):
        """Vista 3/4 (mais estreita) — passo intermediario do giro."""
        half = 3 if facing_right else 3
        cx = ox + 8
        d.ellipse([cx - half - 1, 4, cx + half + 1, 12], fill=BODY, outline=OUTLINE)
        d.ellipse([cx - half, 7, cx + half, 12], fill=BELLY)
        if facing_right:
            d.polygon([(cx + half + 1, 8), (cx + half + 3, 5), (cx + half + 3, 11)], fill=FIN, outline=OUTLINE)
            px(d, cx + 1, 7, EYE_B)
            d.polygon([(cx - 1, 4), (cx + 1, 2), (cx + 2, 4)], fill=FIN)
        else:
            d.polygon([(cx - half - 1, 8), (cx - half - 3, 5), (cx - half - 3, 11)], fill=FIN, outline=OUTLINE)
            px(d, cx - 1, 7, EYE_B)
            d.polygon([(cx + 1, 4), (cx - 1, 2), (cx - 2, 4)], fill=FIN)

    def fish_edge(ox):
        """Vista de frente/traseira (giro: peixe de lado fino)."""
        cx = ox + 8
        d.ellipse([cx - 2, 4, cx + 1, 12], fill=BODY, outline=OUTLINE)
        d.ellipse([cx - 1, 7, cx + 1, 12], fill=BELLY)
        # barbatana no topo
        px(d, cx - 1, 3, FIN)
        px(d, cx, 2, FIN)
        px(d, cx, 3, FIN)
        # cauda embaixo / brilho
        px(d, cx - 1, 5, BODY_D)

    # Frame 0: perfil direito | 1: 3/4 | 2: fio (giro) | 3: 3/4 outro lado
    fish_side(0 * fw, flip=False)
    fish_three_quarter(1 * fw, facing_right=True)
    fish_edge(2 * fw)
    fish_three_quarter(3 * fw, facing_right=False)

    img.save(os.path.join(ASSETS, "sprites", "coin.png"))


# ------------------------------ INIMIGOS ------------------------------------
def blob_frame(d, ox, fw, fh, body, eye, foot=0):
    cx = ox + fw // 2
    d.rectangle([ox + 2, 4, ox + fw - 3, fh - 3], fill=body, outline=OUTLINE)
    # olhos
    px(d, cx - 2, 8, (255, 255, 255, 255)); px(d, cx - 2, 8, eye)
    px(d, cx + 2, 8, (255, 255, 255, 255)); px(d, cx + 2, 8, eye)
    d.rectangle([cx - 3, 8, cx - 1, 10], fill=(255, 255, 255, 255)); px(d, cx - 2, 9, eye)
    d.rectangle([cx + 1, 8, cx + 3, 10], fill=(255, 255, 255, 255)); px(d, cx + 2, 9, eye)
    # pes
    d.rectangle([ox + 3, fh - 3 + foot, ox + 6, fh - 1], fill=OUTLINE)
    d.rectangle([ox + fw - 6, fh - 3 - foot, ox + fw - 3, fh - 1], fill=OUTLINE)


def gen_enemy(path, frames, fw, fh, color):
    img = new_sheet(frames, fw, fh)
    d = ImageDraw.Draw(img)
    for i in range(frames):
        blob_frame(d, i * fw, fw, fh, color, (30, 20, 30, 255), foot=(1 if i % 2 else 0))
    img.save(os.path.join(ASSETS, "sprites", path))


def gen_fast_enemy():
    """Inimigo rapido: homem de bone azul-marinho, oculos pretos retangulares,
    camiseta azul e leve sorriso (imagem-ref). 4 quadros 16x16 para corrida."""
    # Paleta do fast enemy
    F_SKIN = (210, 168, 128, 255)     # pele bronzeada
    F_CAP = (28, 42, 88, 255)         # bone azul-marinho
    F_CAP_BR = (36, 58, 110, 255)     # aba do bone
    F_CAMO_G = (52, 108, 72, 255)     # camuflagem verde na aba
    F_CAMO_B = (44, 88, 130, 255)     # camuflagem azul na aba
    F_LOGO = (168, 220, 188, 255)     # logo montanha/coracao (claro)
    F_GLASS = (18, 18, 22, 255)       # oculos pretos
    F_GLASS_HI = (90, 100, 115, 255)  # reflexo nos oculos
    F_SHIRT = (48, 128, 228, 255)    # camiseta azul
    F_SHIRT_D = (32, 96, 188, 255)    # sombra da camiseta
    F_PANTS = (42, 42, 52, 255)       # calca/short escuro
    F_SHOE = (28, 24, 30, 255)

    fw = fh = 16
    img = new_sheet(4, fw, fh)
    d = ImageDraw.Draw(img)

    def draw_fast_frame(ox, foot=0, lean=0):
        cx = ox + fw // 2
        bob = foot  # leve bob ao correr

        # ---- BONE AZUL-MARINHO + ABA (camuflagem) ----
        cap_y = 0 + bob
        d.rectangle([ox + 3, cap_y, ox + 12, cap_y + 4], fill=F_CAP, outline=OUTLINE)
        # aba projetada
        d.rectangle([ox + 4, cap_y + 3, ox + 13, cap_y + 5], fill=F_CAP_BR, outline=OUTLINE)
        px(d, ox + 6, cap_y + 4, F_CAMO_G)
        px(d, ox + 8, cap_y + 4, F_CAMO_B)
        px(d, ox + 10, cap_y + 4, F_CAMO_G)
        # logo montanha/coracao (simplificado)
        px(d, cx - 1, cap_y + 1, F_LOGO)
        px(d, cx, cap_y + 1, F_LOGO)
        px(d, cx + 1, cap_y + 2, F_LOGO)
        px(d, cx, cap_y, F_LOGO)  # coracao acima

        # ---- ROSTO (pele) + Oculos PRETOS RETANGULARES (marca) ----
        face_y = cap_y + 4
        d.rectangle([ox + 3, face_y, ox + 12, face_y + 5], fill=F_SKIN, outline=OUTLINE)
        # oculos grandes cobrindo olhos
        d.rectangle([ox + 3, face_y + 1, ox + 12, face_y + 4], fill=F_GLASS, outline=OUTLINE)
        # ponte entre lentes
        px(d, cx, face_y + 2, F_GLASS)
        # reflexo (como na foto)
        px(d, ox + 5, face_y + 2, F_GLASS_HI)
        px(d, ox + 10, face_y + 2, F_GLASS_HI)
        # bochechas laterais (pele visivel fora dos oculos)
        px(d, ox + 3, face_y + 3, F_SKIN)
        px(d, ox + 12, face_y + 3, F_SKIN)
        # sorriso leve abaixo dos oculos
        px(d, cx - 1, face_y + 4, (120, 70, 60, 255))
        px(d, cx, face_y + 4, (255, 240, 220, 255))  # dente
        px(d, cx + 1, face_y + 4, (120, 70, 60, 255))

        # ---- CAMISETA AZUL ----
        shirt_y = face_y + 5
        d.rectangle([ox + 2 + lean, shirt_y, ox + 13 - lean, shirt_y + 5], fill=F_SHIRT, outline=OUTLINE)
        px(d, cx, shirt_y + 2, F_SHIRT_D)  # vinco/sombra

        # ---- PERNAS (animacao de corrida rapida) ----
        leg_y = shirt_y + 5
        if foot == 0:
            d.rectangle([ox + 4, leg_y, ox + 6, leg_y + 3], fill=F_PANTS)
            d.rectangle([ox + 9, leg_y + 1, ox + 11, leg_y + 2], fill=F_PANTS)
            d.rectangle([ox + 3, leg_y + 3, ox + 7, leg_y + 3], fill=F_SHOE)
            d.rectangle([ox + 9, leg_y + 2, ox + 12, leg_y + 2], fill=F_SHOE)
        elif foot == 1:
            d.rectangle([ox + 5, leg_y, ox + 7, leg_y + 2], fill=F_PANTS)
            d.rectangle([ox + 8, leg_y, ox + 10, leg_y + 3], fill=F_PANTS)
            d.rectangle([ox + 4, leg_y + 2, ox + 8, leg_y + 2], fill=F_SHOE)
            d.rectangle([ox + 8, leg_y + 3, ox + 11, leg_y + 3], fill=F_SHOE)
        else:
            d.rectangle([ox + 4, leg_y + 1, ox + 6, leg_y + 2], fill=F_PANTS)
            d.rectangle([ox + 9, leg_y, ox + 11, leg_y + 3], fill=F_PANTS)
            d.rectangle([ox + 3, leg_y + 2, ox + 7, leg_y + 2], fill=F_SHOE)
            d.rectangle([ox + 9, leg_y + 3, ox + 12, leg_y + 3], fill=F_SHOE)

    for i in range(4):
        draw_fast_frame(i * fw, foot=(i % 2), lean=(1 if i % 2 else 0))

    img.save(os.path.join(ASSETS, "sprites", "enemy_fast.png"))


def gen_walker_enemy():
    """Walker: CABEÇA do 1o anexo (cabelo curto, oculos roxo/azul reflexivo,
    sorriso largo, barba, pele bronzeada) + CORPO/POSE do 2o anexo (camiseta
    bipartida azul/verde, bermuda amarela, chinelo laranja, garrafa na mao
    esquerda, dedo apontando pra cima, postura cambaleante). 4 frames 24x24."""
    W_SKIN = (200, 150, 110, 255)
    W_SKIN_D = (160, 110, 80, 255)
    W_HAIR = (35, 28, 22, 255)
    W_STUB = (70, 55, 45, 255)
    W_GLASS = (28, 18, 55, 255)        # lente roxa escura
    W_GLASS_HI = (140, 90, 200, 255)   # reflexo roxo/azul
    W_FRAME = (18, 18, 22, 255)
    W_TEETH = (255, 250, 240, 255)
    W_MOUTH = (90, 40, 40, 255)
    W_SHIRT_L = (90, 170, 230, 255)    # azul claro (metade esq)
    W_SHIRT_R = (130, 200, 60, 255)    # verde lima (metade dir)
    W_SHORTS = (240, 170, 40, 255)     # bermuda amarela/laranja
    W_SHOE = (230, 120, 40, 255)       # chinelo laranja
    W_BOTTLE = (96, 52, 22, 255)
    W_BOTTLE_L = (230, 220, 180, 255)

    fw = fh = 24
    img = new_sheet(4, fw, fh)
    d = ImageDraw.Draw(img)

    def draw_walker_frame(ox, foot=0, sway=0):
        cx = ox + fw // 2 + sway
        bob = foot % 2

        # ---- CABEÇA (foto 1) ----
        head_y = 1 + bob
        # cabelo curto (buzz) + laterais
        d.rectangle([ox + 6 + sway, head_y, ox + 17 + sway, head_y + 3], fill=W_HAIR, outline=OUTLINE)
        # rosto
        d.rectangle([ox + 6 + sway, head_y + 2, ox + 17 + sway, head_y + 11], fill=W_SKIN, outline=OUTLINE)
        # orelhas
        px(d, ox + 5 + sway, head_y + 5, W_SKIN)
        px(d, ox + 18 + sway, head_y + 5, W_SKIN)
        # oculos pretos + lentes roxas reflexivas
        d.rectangle([ox + 6 + sway, head_y + 4, ox + 11 + sway, head_y + 7], fill=W_GLASS, outline=W_FRAME)
        d.rectangle([ox + 12 + sway, head_y + 4, ox + 17 + sway, head_y + 7], fill=W_GLASS, outline=W_FRAME)
        px(d, ox + 11 + sway, head_y + 5, W_FRAME)  # ponte
        px(d, ox + 12 + sway, head_y + 5, W_FRAME)
        px(d, ox + 7 + sway, head_y + 5, W_GLASS_HI)
        px(d, ox + 14 + sway, head_y + 5, W_GLASS_HI)
        # nariz
        px(d, cx, head_y + 7, W_SKIN_D)
        # sorriso largo com dentes
        d.rectangle([ox + 8 + sway, head_y + 8, ox + 15 + sway, head_y + 10], fill=W_MOUTH, outline=OUTLINE)
        d.rectangle([ox + 9 + sway, head_y + 8, ox + 14 + sway, head_y + 9], fill=W_TEETH)
        for tx in range(ox + 10 + sway, ox + 14 + sway, 2):
            px(d, tx, head_y + 8, OUTLINE)
        # barba / stubble
        for sx, sy in [(-4, 10), (-2, 10), (0, 10), (2, 10), (4, 10), (-3, 9), (3, 9)]:
            px(d, cx + sx, head_y + sy, W_STUB)

        # ---- BRAÇO DIREITO: dedo apontando pra cima (sprite inimigo01) ----
        # antebraço + mão + dedo índice erguido
        d.rectangle([ox + 18 + sway, head_y + 8, ox + 20 + sway, head_y + 14], fill=W_SKIN, outline=OUTLINE)
        d.rectangle([ox + 19 + sway, head_y + 3, ox + 20 + sway, head_y + 8], fill=W_SKIN, outline=OUTLINE)
        px(d, ox + 19 + sway, head_y + 2, W_SKIN)  # ponta do dedo

        # ---- CORPO: camiseta bipartida azul | verde ----
        body_y = head_y + 11
        d.rectangle([ox + 6 + sway, body_y, cx, body_y + 6], fill=W_SHIRT_L, outline=OUTLINE)
        d.rectangle([cx, body_y, ox + 17 + sway, body_y + 6], fill=W_SHIRT_R, outline=OUTLINE)
        # contorno externo do tronco
        d.rectangle([ox + 6 + sway, body_y, ox + 17 + sway, body_y + 6], outline=OUTLINE)

        # ---- BRAÇO ESQUERDO + GARRAFA (como no sprite refs) ----
        d.rectangle([ox + 3 + sway, body_y + 1, ox + 5 + sway, body_y + 5], fill=W_SKIN, outline=OUTLINE)
        # garrafa inclinada
        bx, by = ox + 1 + sway, body_y + 2
        d.rectangle([bx, by, bx + 2, by + 6], fill=W_BOTTLE, outline=OUTLINE)
        px(d, bx + 1, by - 1, W_BOTTLE)  # gargalo
        d.rectangle([bx, by + 2, bx + 2, by + 4], fill=W_BOTTLE_L)

        # ---- BERMUDA AMARELA + PERNAS (camaleantes / andar) ----
        shorts_y = body_y + 6
        d.rectangle([ox + 6 + sway, shorts_y, ox + 17 + sway, shorts_y + 3], fill=W_SHORTS, outline=OUTLINE)
        leg_y = shorts_y + 3
        if foot % 2 == 0:
            d.rectangle([ox + 7 + sway, leg_y, ox + 10 + sway, leg_y + 3], fill=W_SKIN, outline=OUTLINE)
            d.rectangle([ox + 13 + sway, leg_y + 1, ox + 16 + sway, leg_y + 2], fill=W_SKIN, outline=OUTLINE)
            d.rectangle([ox + 6 + sway, leg_y + 3, ox + 11 + sway, leg_y + 3], fill=W_SHOE)
            d.rectangle([ox + 13 + sway, leg_y + 2, ox + 17 + sway, leg_y + 2], fill=W_SHOE)
        else:
            d.rectangle([ox + 7 + sway, leg_y + 1, ox + 10 + sway, leg_y + 2], fill=W_SKIN, outline=OUTLINE)
            d.rectangle([ox + 13 + sway, leg_y, ox + 16 + sway, leg_y + 3], fill=W_SKIN, outline=OUTLINE)
            d.rectangle([ox + 6 + sway, leg_y + 2, ox + 11 + sway, leg_y + 2], fill=W_SHOE)
            d.rectangle([ox + 13 + sway, leg_y + 3, ox + 17 + sway, leg_y + 3], fill=W_SHOE)

    # 4 frames: sway + troca de passos = andar "bebado"/cambaleante
    for i, (foot, sway) in enumerate([(0, 0), (1, 1), (0, 0), (1, -1)]):
        draw_walker_frame(i * fw, foot=foot, sway=sway)

    img.save(os.path.join(ASSETS, "sprites", "enemy_walker.png"))


def gen_flyer_enemy():
    """Inimigo voador: homem de bone Nike preto (swoosh branco), goatee
    sal-e-pimenta, pele clara e camisa preta (imagem-ref). 2 quadros 16x16
    com asas laterais batendo (voador)."""
    V_SKIN = (228, 188, 152, 255)      # pele clara
    V_SKIN_D = (190, 150, 118, 255)    # sombra
    V_CAP = (28, 28, 32, 255)          # bone preto/carvao
    V_CAP_BR = (18, 18, 22, 255)       # aba
    V_SWOOSH = (245, 245, 250, 255)    # logo swoosh branco
    V_HAIR_D = (55, 45, 40, 255)       # barba escura
    V_HAIR_G = (170, 170, 175, 255)    # grisalho (sal-e-pimenta)
    V_SHIRT = (22, 22, 26, 255)        # camisa preta
    V_WING = (210, 215, 225, 255)      # asa
    V_WING_D = (150, 155, 170, 255)

    fw = fh = 16
    img = new_sheet(2, fw, fh)
    d = ImageDraw.Draw(img)

    def draw_flyer_frame(ox, wing_up=False):
        cx = ox + fw // 2

        # ---- ASAS LATERAIS (batem entre frames) ----
        if wing_up:
            # asas levantadas
            d.polygon([(ox + 1, 5), (ox + 3, 2), (ox + 4, 7)], fill=V_WING, outline=OUTLINE)
            d.polygon([(ox + 14, 5), (ox + 12, 2), (ox + 11, 7)], fill=V_WING, outline=OUTLINE)
            px(d, ox + 2, 4, V_WING_D)
            px(d, ox + 13, 4, V_WING_D)
        else:
            # asas abaixadas
            d.polygon([(ox + 1, 8), (ox + 3, 11), (ox + 4, 6)], fill=V_WING, outline=OUTLINE)
            d.polygon([(ox + 14, 8), (ox + 12, 11), (ox + 11, 6)], fill=V_WING, outline=OUTLINE)
            px(d, ox + 2, 9, V_WING_D)
            px(d, ox + 13, 9, V_WING_D)

        # ---- BONE PRETO + SWOOSH BRANCO (Nike-like) ----
        d.rectangle([ox + 4, 1, ox + 11, 4], fill=V_CAP, outline=OUTLINE)
        d.rectangle([ox + 5, 3, ox + 12, 5], fill=V_CAP_BR, outline=OUTLINE)  # aba
        # swoosh: curva simples branca
        px(d, ox + 6, 2, V_SWOOSH)
        px(d, ox + 7, 3, V_SWOOSH)
        px(d, ox + 8, 3, V_SWOOSH)
        px(d, ox + 9, 2, V_SWOOSH)

        # ---- ROSTO + ORELHAS ----
        d.rectangle([ox + 4, 4, ox + 11, 10], fill=V_SKIN, outline=OUTLINE)
        px(d, ox + 3, 6, V_SKIN)   # orelha esq
        px(d, ox + 12, 6, V_SKIN)  # orelha dir
        # olhos castanhos escuros, olhar serio
        px(d, ox + 6, 6, EYE)
        px(d, ox + 9, 6, EYE)
        # costeletas grisalhas
        px(d, ox + 4, 7, V_HAIR_G)
        px(d, ox + 11, 7, V_HAIR_G)
        # nariz
        px(d, cx, 7, V_SKIN_D)

        # ---- GOATEE SAL-E-PIMENTA (marca do personagem) ----
        # bigode
        px(d, ox + 6, 8, V_HAIR_D)
        px(d, ox + 7, 8, V_HAIR_G)
        px(d, ox + 8, 8, V_HAIR_D)
        px(d, ox + 9, 8, V_HAIR_G)
        # queixo / cavanhaque
        px(d, ox + 7, 9, V_HAIR_D)
        px(d, ox + 8, 9, V_HAIR_G)
        px(d, ox + 7, 10, V_HAIR_G)
        px(d, ox + 8, 10, V_HAIR_D)

        # ---- CAMISA PRETA (corpo curto, voador) ----
        d.rectangle([ox + 4, 10, ox + 11, 14], fill=V_SHIRT, outline=OUTLINE)
        # pescoco
        px(d, cx, 10, V_SKIN)

    draw_flyer_frame(0, wing_up=True)
    draw_flyer_frame(fw, wing_up=False)
    img.save(os.path.join(ASSETS, "sprites", "enemy_flyer.png"))


def gen_tank_enemy():
    """Inimigo tank (resistente): homem de chapeu bucket azul com emblema PE
    (arco-iris + cruz + sol), oculos pretos Wayfarer, sorriso largo e
    camiseta rosa (imagem-ref). 2 quadros 24x24, corpo robusto."""
    T_SKIN = (222, 176, 138, 255)      # pele bronzeada
    T_SKIN_D = (186, 140, 108, 255)    # sombra / barba
    T_HAT = (40, 110, 210, 255)        # bucket hat azul vivo
    T_HAT_D = (28, 80, 165, 255)       # sombra da aba
    T_GLASS = (18, 18, 22, 255)        # oculos pretos
    T_GLASS_HI = (95, 105, 120, 255)   # reflexo
    T_TEETH = (255, 250, 240, 255)
    T_MOUTH = (90, 40, 40, 255)
    T_SHIRT = (220, 150, 170, 255)     # camiseta rosa/mauve
    T_SHIRT_D = (180, 110, 135, 255)
    T_PANTS = (50, 55, 70, 255)
    T_SHOE = (30, 28, 32, 255)
    # emblem PE: rainbow + red cross + yellow sun
    PE_R = (220, 50, 50, 255)
    PE_Y = (245, 200, 40, 255)
    PE_G = (50, 180, 70, 255)
    PE_B = (60, 100, 220, 255)

    fw = fh = 24
    img = new_sheet(2, fw, fh)
    d = ImageDraw.Draw(img)

    def draw_tank_frame(ox, foot=0):
        cx = ox + fw // 2
        bob = foot

        # ---- BUCKET HAT AZUL (aba larga, ondulada) ----
        hat_y = 1 + bob
        # topo do chapeu
        d.ellipse([ox + 5, hat_y, ox + 18, hat_y + 8], fill=T_HAT, outline=OUTLINE)
        d.rectangle([ox + 5, hat_y + 4, ox + 18, hat_y + 7], fill=T_HAT)
        # aba larga
        d.ellipse([ox + 2, hat_y + 5, ox + 21, hat_y + 11], fill=T_HAT_D, outline=OUTLINE)
        d.rectangle([ox + 3, hat_y + 6, ox + 20, hat_y + 9], fill=T_HAT)
        # emblema PE (simplificado no centro)
        px(d, cx - 1, hat_y + 3, PE_R)
        px(d, cx, hat_y + 3, PE_Y)
        px(d, cx + 1, hat_y + 3, PE_G)
        px(d, cx, hat_y + 2, PE_B)       # sol/arco
        px(d, cx, hat_y + 4, PE_R)       # cruz

        # ---- ROSTO ----
        face_y = hat_y + 8
        d.rectangle([ox + 5, face_y, ox + 18, face_y + 9], fill=T_SKIN, outline=OUTLINE)
        # orelhas
        px(d, ox + 4, face_y + 3, T_SKIN)
        px(d, ox + 19, face_y + 3, T_SKIN)

        # ---- OCULOS WAYFARER PRETOS (sobre a pele, nao cobrindo tudo) ----
        d.rectangle([ox + 6, face_y + 1, ox + 10, face_y + 4], fill=T_GLASS, outline=OUTLINE)
        d.rectangle([ox + 13, face_y + 1, ox + 17, face_y + 4], fill=T_GLASS, outline=OUTLINE)
        px(d, cx - 1, face_y + 2, OUTLINE)
        px(d, cx, face_y + 2, OUTLINE)  # ponte
        px(d, ox + 7, face_y + 2, T_GLASS_HI)
        px(d, ox + 14, face_y + 2, T_GLASS_HI)
        # bochechas visiveis
        px(d, ox + 5, face_y + 3, T_SKIN)
        px(d, ox + 18, face_y + 3, T_SKIN)

        # ---- SORRISO LARGO COM DENTES ----
        mouth_y = face_y + 5
        d.rectangle([ox + 7, mouth_y, ox + 16, mouth_y + 3], fill=T_MOUTH, outline=OUTLINE)
        d.rectangle([ox + 8, mouth_y + 1, ox + 15, mouth_y + 2], fill=T_TEETH)
        for tx in range(ox + 9, ox + 15, 2):
            px(d, tx, mouth_y + 1, OUTLINE)
        # cantos erguidos
        px(d, ox + 6, mouth_y + 1, T_SKIN_D)
        px(d, ox + 17, mouth_y + 1, T_SKIN_D)

        # ---- BARBA RALA / STUBBLE ----
        for sx, sy in [(-4, 8), (-2, 8), (0, 8), (2, 8), (4, 8), (-3, 7), (3, 7)]:
            px(d, cx + sx, face_y + sy, T_SKIN_D)

        # ---- CORPO ROBUSTO (camiseta rosa) ----
        body_y = face_y + 9
        d.rectangle([ox + 3, body_y, ox + 20, fh - 5], fill=T_SHIRT, outline=OUTLINE)
        # gola
        d.rectangle([ox + 9, body_y, ox + 14, body_y + 1], fill=T_SKIN)
        px(d, cx, body_y + 4, T_SHIRT_D)
        # bracos grossos
        d.rectangle([ox + 1, body_y + 1, ox + 3, body_y + 6], fill=T_SKIN, outline=OUTLINE)
        d.rectangle([ox + 20, body_y + 1, ox + 22, body_y + 6], fill=T_SKIN, outline=OUTLINE)

        # ---- PERNAS CURTAS (tank = robusto / baixo) ----
        leg_y = fh - 5
        if foot == 0:
            d.rectangle([ox + 5, leg_y, ox + 9, leg_y + 3], fill=T_PANTS)
            d.rectangle([ox + 14, leg_y + 1, ox + 18, leg_y + 2], fill=T_PANTS)
            d.rectangle([ox + 4, leg_y + 3, ox + 10, leg_y + 3], fill=T_SHOE)
            d.rectangle([ox + 14, leg_y + 2, ox + 19, leg_y + 2], fill=T_SHOE)
        else:
            d.rectangle([ox + 5, leg_y + 1, ox + 9, leg_y + 2], fill=T_PANTS)
            d.rectangle([ox + 14, leg_y, ox + 18, leg_y + 3], fill=T_PANTS)
            d.rectangle([ox + 4, leg_y + 2, ox + 10, leg_y + 2], fill=T_SHOE)
            d.rectangle([ox + 14, leg_y + 3, ox + 19, leg_y + 3], fill=T_SHOE)

    draw_tank_frame(0, foot=0)
    draw_tank_frame(fw, foot=1)
    img.save(os.path.join(ASSETS, "sprites", "enemy_tank.png"))


def gen_boss():
    """Boss: homem de boné claro, pele escura e SORRISO MALICIOSO (imagem-ref).
    Cabeça grande (marca de chefe 16 bits), camisa preta, 2 frames de 'andar'.
    Dimensão 48x48 inalterada (Assets.java / Boss.java)."""
    # Paleta do boss (não reutilizar as cores claras do Daniel).
    B_SKIN = (148, 96, 68, 255)       # pele morena
    B_SKIN_D = (112, 68, 44, 255)     # sombra / barba rala
    B_CAP = (236, 236, 240, 255)      # boné branco
    B_CAP_S = (200, 200, 208, 255)    # sombra do boné
    B_BROW = (42, 30, 24, 255)        # sobrancelhas
    B_STUB = (70, 48, 36, 255)        # barba / stubble
    B_TEETH = (255, 250, 240, 255)    # dentes brancos
    B_MOUTH = (55, 22, 22, 255)       # interior da boca
    B_SHIRT = (28, 28, 34, 255)       # camisa preta
    B_SHOE = (20, 18, 22, 255)

    fw = fh = 48
    img = new_sheet(2, fw, fh)
    d = ImageDraw.Draw(img)

    for i in range(2):
        ox = i * fw
        cx = ox + fw // 2
        bob = i  # leve "bob" entre frames (andar)

        # ---- BONÉ DE BASEBALL (topo + aba) ----
        cap_y = 2 + bob
        d.ellipse([cx - 14, cap_y, cx + 13, cap_y + 14], fill=B_CAP, outline=OUTLINE)
        d.rectangle([cx - 14, cap_y + 6, cx + 13, cap_y + 12], fill=B_CAP)
        # aba do boné projetada à frente (direita no sprite)
        d.ellipse([cx + 2, cap_y + 8, cx + 18, cap_y + 14], fill=B_CAP_S, outline=OUTLINE)
        d.rectangle([cx + 2, cap_y + 10, cx + 17, cap_y + 13], fill=B_CAP_S)
        # botão / costura no topo
        px(d, cx - 1, cap_y + 3, B_CAP_S)
        px(d, cx, cap_y + 3, B_CAP_S)

        # ---- ROSTO REDONDO (pele escura) ----
        face_y = cap_y + 8
        d.ellipse([cx - 13, face_y, cx + 12, face_y + 24], fill=B_SKIN, outline=OUTLINE)
        # sombra sob o boné (testa)
        d.rectangle([cx - 11, face_y + 1, cx + 10, face_y + 4], fill=B_SKIN_D)

        # ---- SOBRANCELHAS GROSSAS (arqueadas / maliciosas) ----
        brow_y = face_y + 5
        d.rectangle([cx - 9, brow_y, cx - 3, brow_y + 2], fill=B_BROW)
        d.rectangle([cx + 2, brow_y, cx + 8, brow_y + 2], fill=B_BROW)
        px(d, cx - 3, brow_y - 1, B_BROW)
        px(d, cx + 2, brow_y - 1, B_BROW)
        px(d, cx - 10, brow_y + 1, B_BROW)
        px(d, cx + 9, brow_y + 1, B_BROW)

        # ---- OLHOS ENTREFECHADOS (fenda, sorriso forte) ----
        eye_y = brow_y + 3
        # "saco" abaixo dos olhos
        d.rectangle([cx - 8, eye_y + 1, cx - 3, eye_y + 2], fill=B_SKIN_D)
        d.rectangle([cx + 2, eye_y + 1, cx + 7, eye_y + 2], fill=B_SKIN_D)
        # fendas brancas + pupilas
        d.rectangle([cx - 8, eye_y, cx - 3, eye_y], fill=(240, 230, 210, 255))
        d.rectangle([cx + 2, eye_y, cx + 7, eye_y], fill=(240, 230, 210, 255))
        d.rectangle([cx - 7, eye_y, cx - 4, eye_y], fill=EYE)
        d.rectangle([cx + 3, eye_y, cx + 6, eye_y], fill=EYE)

        # ---- NARIZ LARGO ----
        d.rectangle([cx - 2, eye_y + 2, cx + 2, eye_y + 5], fill=B_SKIN_D)
        px(d, cx - 2, eye_y + 5, B_BROW)
        px(d, cx + 2, eye_y + 5, B_BROW)

        # ---- SORRISO MALICIOSO LARGO (marca do personagem) ----
        # Boca aberta em arco: fileira de dentes brancos em cima + interior escuro embaixo.
        mouth_y = eye_y + 7
        # contorno externo da boca (arco largo)
        d.ellipse([cx - 12, mouth_y, cx + 11, mouth_y + 11], fill=B_MOUTH, outline=OUTLINE)
        # cobrir a metade superior com pele -> sobra o "U" do sorriso
        d.rectangle([cx - 12, mouth_y - 1, cx + 11, mouth_y + 3], fill=B_SKIN)
        # labio superior / linha do sorriso
        d.rectangle([cx - 11, mouth_y + 2, cx + 10, mouth_y + 3], fill=OUTLINE)
        # DENTES SUPERIORES (fileira branca bem visivel)
        d.rectangle([cx - 10, mouth_y + 3, cx + 9, mouth_y + 6], fill=B_TEETH)
        for tx in range(cx - 9, cx + 9, 3):
            px(d, tx, mouth_y + 3, OUTLINE)
            px(d, tx, mouth_y + 4, OUTLINE)
        # cantos do sorriso erguidos (malicioso)
        px(d, cx - 12, mouth_y + 4, B_SKIN)
        px(d, cx + 11, mouth_y + 4, B_SKIN)
        px(d, cx - 11, mouth_y + 3, B_SKIN)
        px(d, cx + 10, mouth_y + 3, B_SKIN)
        # labio inferior
        d.rectangle([cx - 8, mouth_y + 9, cx + 7, mouth_y + 10], fill=B_SKIN_D)

        # ---- BARBA / STUBBLE (queixo + bigode + maxilar) ----
        for sx, sy in [
            (-10, 14), (-9, 15), (-8, 16), (-7, 17), (-6, 17), (-5, 18),
            (-4, 18), (-3, 18), (-2, 19), (-1, 19), (0, 19), (1, 19),
            (2, 19), (3, 18), (4, 18), (5, 18), (6, 17), (7, 17),
            (8, 16), (9, 15), (10, 14),
            (-8, 15), (6, 15), (-4, 17), (2, 17), (-2, 17), (4, 16),
        ]:
            px(d, cx + sx, face_y + sy, B_STUB)
        # bigode sob o nariz (acima dos dentes)
        for mx in range(-6, 7):
            if mx not in (0,):
                px(d, cx + mx, mouth_y + 1, B_STUB)
                px(d, cx + mx, mouth_y + 2, B_STUB)

        # ---- ORELHAS ----
        d.ellipse([cx - 15, face_y + 6, cx - 12, face_y + 12], fill=B_SKIN, outline=OUTLINE)
        d.ellipse([cx + 11, face_y + 6, cx + 14, face_y + 12], fill=B_SKIN, outline=OUTLINE)

        # ---- PESCOÇO + CAMISA PRETA ----
        neck_y = face_y + 22
        d.rectangle([cx - 5, neck_y, cx + 4, neck_y + 4], fill=B_SKIN, outline=OUTLINE)
        # ombros / camisa
        d.rectangle([cx - 14, neck_y + 3, cx + 13, fh - 6], fill=B_SHIRT, outline=OUTLINE)
        # gola em V
        d.polygon([
            (cx - 4, neck_y + 3),
            (cx, neck_y + 9),
            (cx + 4, neck_y + 3),
        ], fill=B_SKIN, outline=OUTLINE)

        # ---- BRAÇOS (animação leve entre frames) ----
        arm_shift = 1 if i == 0 else -1
        d.rectangle([ox + 4, neck_y + 6 + arm_shift, ox + 8, fh - 8], fill=B_SKIN, outline=OUTLINE)
        d.rectangle([ox + fw - 9, neck_y + 6 - arm_shift, ox + fw - 5, fh - 8], fill=B_SKIN, outline=OUTLINE)

        # ---- PERNAS / PÉS ----
        foot_y = fh - 5
        d.rectangle([cx - 10, foot_y + (0 if i == 0 else 1), cx - 3, fh - 1], fill=B_SHOE)
        d.rectangle([cx + 2, foot_y + (1 if i == 0 else 0), cx + 9, fh - 1], fill=B_SHOE)

    img.save(os.path.join(ASSETS, "sprites", "boss.png"))


def gen_castle():
    """Barraca/"castelo" de fim de fase — inspirada no boteco de praia amarelo/
    vermelho (toldo branco, balcoes, porta Sanitario, letra PITU + camarao).
    Tamanho unico 80x56 (sprites/castle.png)."""
    w, h = 80, 56
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    YELLOW = (255, 220, 40, 255)
    YELLOW_D = (230, 180, 20, 255)
    RED = (210, 40, 35, 255)
    RED_D = (150, 25, 25, 255)
    WHITE = (250, 250, 250, 255)
    BLACK = (25, 20, 20, 255)
    GREEN = (40, 160, 60, 255)
    BLUE = (40, 110, 180, 255)
    ORANGE = (255, 140, 40, 255)
    DARK = (60, 50, 40, 255)

    # ---- BASE / PLATAFORMA ----
    d.rectangle([2, 48, 77, 55], fill=DARK, outline=OUTLINE)

    # ---- CORPO AMARELO ----
    d.rectangle([4, 14, 75, 48], fill=YELLOW, outline=OUTLINE)
    # sombra lateral direita
    d.rectangle([70, 14, 75, 48], fill=YELLOW_D)

    # ---- FAIXA INFERIOR VERMELHA COM ONDA ----
    d.rectangle([4, 40, 75, 48], fill=RED)
    for x in range(6, 74, 6):
        d.polygon([(x, 42), (x + 3, 40), (x + 6, 42), (x + 3, 44)], fill=ORANGE)

    # ---- TOLDO BRANCO (awning) ----
    d.polygon([(2, 12), (78, 12), (74, 6), (6, 6)], fill=WHITE, outline=OUTLINE)
    # listras do toldo
    for x in range(10, 72, 8):
        d.line([x, 7, x + 2, 12], fill=(220, 220, 230, 255))
    # sombra sob o toldo
    d.rectangle([4, 12, 75, 14], fill=YELLOW_D)

    # ---- DOIS BALCOES ABERTOS (janelas de atendimento) ----
    # balcao 1
    d.rectangle([8, 18, 28, 36], fill=(30, 30, 35, 255), outline=OUTLINE)
    d.rectangle([8, 33, 28, 36], fill=DARK)  # bancada
    # balcao 2
    d.rectangle([32, 18, 52, 36], fill=(30, 30, 35, 255), outline=OUTLINE)
    d.rectangle([32, 33, 52, 36], fill=DARK)

    # ---- PORTA VERMELHA (Sanitario) ----
    d.rectangle([56, 18, 70, 48], fill=RED, outline=OUTLINE)
    d.rectangle([58, 20, 68, 30], fill=RED_D)  # painel superior
    # texto simplificado "SAN" / faixa
    d.rectangle([58, 21, 68, 23], fill=WHITE)
    px(d, 60, 22, BLACK)
    px(d, 63, 22, BLACK)
    px(d, 66, 22, BLACK)
    # macaneta
    px(d, 67, 34, YELLOW)
    # "PROIBIDO FUMAR" vertical (pontinhos)
    for yy in range(26, 44, 3):
        px(d, 59, yy, BLACK)

    # ---- LETREIRO PITU + CAMARAO (frente da faixa) ----
    # letras P I T U em vermelho gordas
    # P
    d.rectangle([10, 41, 13, 47], fill=RED_D)
    d.rectangle([10, 41, 15, 43], fill=RED_D)
    d.rectangle([13, 41, 15, 44], fill=RED_D)
    # I
    d.rectangle([17, 41, 19, 47], fill=RED_D)
    # T
    d.rectangle([21, 41, 27, 42], fill=RED_D)
    d.rectangle([23, 42, 25, 47], fill=RED_D)
    # U
    d.rectangle([29, 41, 31, 46], fill=RED_D)
    d.rectangle([33, 41, 35, 46], fill=RED_D)
    d.rectangle([29, 45, 35, 47], fill=RED_D)
    # accento no U + camarao
    px(d, 34, 40, RED_D)
    # camarao pequeno (verde/azul)
    d.ellipse([37, 41, 43, 46], fill=GREEN, outline=OUTLINE)
    px(d, 42, 42, BLUE)
    px(d, 38, 43, (255, 100, 100, 255))  # olho/antena

    # ---- DETALHES: numero 136, cartaz, "AMBIENTE" ----
    # 136 no pilar
    px(d, 54, 20, BLACK)
    px(d, 54, 22, BLACK)
    px(d, 54, 24, BLACK)
    # cartaz (Itaipava-like simplificado)
    d.rectangle([34, 20, 48, 30], fill=(50, 100, 50, 255), outline=OUTLINE)
    d.rectangle([36, 22, 40, 28], fill=(200, 220, 80, 255))  # garrafa
    # simbolo nao fumar
    d.ellipse([10, 20, 16, 26], outline=RED)
    px(d, 13, 23, RED)

    # tipografia "AMBIENTE" (linha de pixels)
    for x in range(10, 30, 2):
        px(d, x, 38, BLACK)

    img.save(os.path.join(ASSETS, "sprites", "castle.png"))


# ------------------------------- TILESET ------------------------------------
def gen_tileset():
    # 8 tiles 16x16; id0 transparente.
    img = Image.new("RGBA", (16 * 8, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    def tile(idx):
        return idx * 16

    # 1 GRASS (topo verde + terra)
    x = tile(1)
    d.rectangle([x, 0, x + 15, 15], fill=(120, 72, 40, 255))
    d.rectangle([x, 0, x + 15, 4], fill=(70, 170, 70, 255))
    d.rectangle([x, 4, x + 15, 5], fill=(40, 120, 45, 255))

    # 2 DIRT
    x = tile(2)
    d.rectangle([x, 0, x + 15, 15], fill=(120, 72, 40, 255))
    for yy in range(0, 16, 4):
        for xx in range(0, 16, 4):
            px(d, x + xx, yy, (100, 58, 32, 255))

    # 3 BRICK (quebravel)
    x = tile(3)
    d.rectangle([x, 0, x + 15, 15], fill=(170, 80, 60, 255))
    d.line([x, 8, x + 15, 8], fill=(90, 40, 30, 255))
    d.line([x + 8, 0, x + 8, 7], fill=(90, 40, 30, 255))
    d.line([x + 4, 8, x + 4, 15], fill=(90, 40, 30, 255))
    d.line([x + 12, 8, x + 12, 15], fill=(90, 40, 30, 255))

    # 4 PLATFORM
    x = tile(4)
    d.rectangle([x, 2, x + 15, 9], fill=(150, 110, 60, 255), outline=(90, 60, 30, 255))
    d.rectangle([x, 2, x + 15, 4], fill=(190, 150, 90, 255))

    # 5 SPIKE (perigo)
    x = tile(5)
    for t in range(0, 16, 4):
        d.polygon([x + t, 15, x + t + 2, 4, x + t + 4, 15], fill=(200, 200, 210, 255), outline=(80, 80, 90, 255))

    # 6 STONE
    x = tile(6)
    d.rectangle([x, 0, x + 15, 15], fill=(110, 110, 120, 255), outline=(70, 70, 80, 255))
    d.rectangle([x + 3, 3, x + 12, 12], fill=(130, 130, 140, 255))

    # 7 DECO (flor/arbusto)
    x = tile(7)
    d.rectangle([x + 6, 8, x + 9, 15], fill=(60, 130, 55, 255))
    d.ellipse([x + 3, 3, x + 12, 10], fill=(90, 190, 90, 255))
    px(d, x + 7, 6, (255, 240, 120, 255))

    img.save(os.path.join(ASSETS, "tiles", "tileset.png"))


# ------------------------------ FUNDOS (praia em TODAS as fases) ------------
# Variantes de iluminacao da mesma cena de praia (manha → meio-dia → tarde →
# por-do-sol → crepusculo), mantendo identidade visual propria por fase.
BEACH_LIGHTING = [
    # (sky_top, sky_horizon, ocean, ocean_deep, sand, sand_dark, sun)
    ((135, 200, 255), (200, 230, 255), (40, 140, 200), (20, 90, 150), (236, 210, 150), (210, 175, 120), (255, 245, 180)),  # 1 manha
    ((90, 180, 250), (170, 220, 255), (30, 130, 210), (15, 80, 150), (242, 220, 160), (220, 185, 125), (255, 250, 200)),  # 2 meio-dia
    ((110, 190, 240), (255, 220, 170), (35, 135, 195), (20, 85, 145), (240, 205, 145), (215, 170, 115), (255, 230, 140)),  # 3 tarde
    ((255, 140, 90), (255, 200, 120), (50, 110, 170), (25, 70, 120), (255, 190, 130), (220, 150, 100), (255, 200, 80)),   # 4 por-do-sol
    ((60, 70, 120), (120, 90, 140), (30, 70, 120), (15, 40, 80), (160, 140, 120), (120, 100, 85), (240, 230, 200)),      # 5 crepusculo
]


def gen_background(idx):
    """Fundo 256x224 de PRAIA (ceu + mar + areia + ondas + palmeiras/nuvens).
    Cada fase muda so a iluminacao (BEACH_LIGHTING), nao o tema."""
    w, h = 256, 224
    sky_t, sky_h, ocean, ocean_d, sand, sand_d, sun = BEACH_LIGHTING[idx % len(BEACH_LIGHTING)]
    img = Image.new("RGBA", (w, h), (0, 0, 0, 255))
    d = ImageDraw.Draw(img)

    # Faixas: ceu (0..120), mar (120..170), areia (170..224)
    sky_end = 120
    sea_end = 170

    # ---- CEU (gradiente) ----
    for y in range(sky_end):
        t = y / max(1, sky_end - 1)
        r = int(sky_t[0] * (1 - t) + sky_h[0] * t)
        g = int(sky_t[1] * (1 - t) + sky_h[1] * t)
        b = int(sky_t[2] * (1 - t) + sky_h[2] * t)
        d.line([0, y, w, y], fill=(r, g, b, 255))

    # sol (posicao e tamanho levemente diferentes por fase)
    sx = 200 - idx * 18
    sy = 18 + idx * 6
    sr = 14 + (idx % 3)
    d.ellipse([sx - sr, sy - sr, sx + sr, sy + sr], fill=sun)
    # reflexo leve do sol
    d.ellipse([sx - sr // 2, sy - sr // 2, sx + sr // 2, sy + sr // 2],
              fill=(255, 255, 255, 80) if idx < 4 else (255, 255, 220, 60))

    # nuvens (simples)
    cloud = (255, 255, 255, 220) if idx < 4 else (180, 180, 200, 200)
    for cx, cy in [(20, 30), (70, 22), (140, 35), (210, 28)]:
        cx2 = (cx + idx * 15) % (w - 40)
        d.ellipse([cx2, cy, cx2 + 28, cy + 12], fill=cloud)
        d.ellipse([cx2 + 10, cy - 4, cx2 + 34, cy + 10], fill=cloud)

    # ---- MAR ----
    for y in range(sky_end, sea_end):
        t = (y - sky_end) / max(1, sea_end - sky_end - 1)
        r = int(ocean[0] * (1 - t) + ocean_d[0] * t)
        g = int(ocean[1] * (1 - t) + ocean_d[1] * t)
        b = int(ocean[2] * (1 - t) + ocean_d[2] * t)
        d.line([0, y, w, y], fill=(r, g, b, 255))

    # ondas (linhas claras)
    foam = (220, 240, 255, 255) if idx < 4 else (180, 200, 220, 255)
    for i, oy in enumerate([sky_end + 8, sky_end + 18, sky_end + 30, sea_end - 6]):
        for x in range(0, w, 16):
            phase = (x // 8 + i + idx) % 4
            d.arc([x, oy - 2 + phase, x + 14, oy + 4 + phase], 0, 180, fill=foam)

    # ---- AREIA ----
    for y in range(sea_end, h):
        t = (y - sea_end) / max(1, h - sea_end - 1)
        r = int(sand[0] * (1 - t) + sand_d[0] * t)
        g = int(sand[1] * (1 - t) + sand_d[1] * t)
        b = int(sand[2] * (1 - t) + sand_d[2] * t)
        d.line([0, y, w, y], fill=(r, g, b, 255))

    # marca d'agua / espuma na beira
    d.rectangle([0, sea_end - 2, w, sea_end + 3], fill=foam)

    # textura da areia (pontinhos)
    grit = (sand_d[0] - 20, sand_d[1] - 15, sand_d[2] - 10, 255)
    for gy in range(sea_end + 6, h, 5):
        for gx in range((gy + idx * 3) % 5, w, 7):
            px(d, gx, gy, grit)

    # ---- PALMEIRAS (silhueta nas laterais; na fase do boss fica uma so a esquerda) ----
    trunk = (90, 60, 35, 255) if idx < 4 else (50, 35, 25, 255)
    leaves = (40, 130, 55, 255) if idx < 4 else (30, 70, 45, 255)
    palm_xs = (18,) if idx == 4 else (18, 238)
    for base_x in palm_xs:
        d.rectangle([base_x - 2, sea_end - 50, base_x + 1, sea_end + 8], fill=trunk)
        for ang in (-40, -15, 15, 40):
            lx = base_x + int(22 * math.sin(math.radians(ang)))
            ly = sea_end - 55 + int(10 * abs(math.cos(math.radians(ang))))
            d.polygon([
                (base_x, sea_end - 52),
                (lx, ly - 6),
                (lx + (2 if ang > 0 else -2), ly + 4),
            ], fill=leaves)

    if idx == 4:
        # ---- IATE GRANDE (fase do boss) — atracado no horizonte ----
        draw_yacht(d, w, sky_end, sea_end)
    else:
        # jangada pequena no horizonte (detalhe Brasilia Teimosa)
        jx = 100 + idx * 20
        boat = (120, 80, 50, 255)
        d.polygon([(jx, sky_end + 20), (jx + 18, sky_end + 20), (jx + 14, sky_end + 26), (jx + 4, sky_end + 26)], fill=boat)
        d.line([jx + 9, sky_end + 8, jx + 9, sky_end + 20], fill=(200, 200, 210, 255))

    img.save(os.path.join(ASSETS, "textures", "bg_stage%d.png" % (idx + 1)))


def draw_yacht(d, w, sky_end, sea_end):
    """Desenha um IATE GRANDE no mar (pixel art), usado no fundo da fase do boss."""
    # Posicao: ocupa boa parte da largura, casco na água.
    x0 = 48
    waterline = sky_end + 28  # linha da agua no casco

    HULL = (230, 235, 245, 255)       # casco branco
    HULL_D = (180, 190, 210, 255)     # sombra
    STRIPE = (30, 90, 170, 255)       # faixa azul
    DECK = (210, 215, 225, 255)
    WINDOW = (80, 160, 220, 255)
    WINDOW_L = (180, 220, 255, 255)
    MAST = (60, 60, 70, 255)
    LIGHT = (255, 240, 120, 255)
    OUT = (25, 20, 30, 255)

    # --- Casco (trapezio largo) ---
    d.polygon([
        (x0 + 8, waterline),
        (x0 + 150, waterline),
        (x0 + 140, waterline + 18),
        (x0 + 20, waterline + 18),
    ], fill=HULL, outline=OUT)
    # quilha / sombra inferior
    d.polygon([
        (x0 + 22, waterline + 18),
        (x0 + 138, waterline + 18),
        (x0 + 125, waterline + 26),
        (x0 + 35, waterline + 26),
    ], fill=HULL_D, outline=OUT)
    # faixa azul no casco
    d.rectangle([x0 + 22, waterline + 10, x0 + 136, waterline + 13], fill=STRIPE)

    # --- Convés / salão superior ---
    d.rectangle([x0 + 30, waterline - 18, x0 + 120, waterline], fill=DECK, outline=OUT)
    # 2o andar (bridge)
    d.rectangle([x0 + 48, waterline - 32, x0 + 105, waterline - 18], fill=HULL, outline=OUT)
    # flybridge / teto
    d.rectangle([x0 + 55, waterline - 40, x0 + 98, waterline - 32], fill=DECK, outline=OUT)

    # --- Janelas (fileiras) ---
    for wx in range(x0 + 36, x0 + 115, 12):
        d.rectangle([wx, waterline - 14, wx + 8, waterline - 6], fill=WINDOW, outline=OUT)
        px(d, wx + 2, waterline - 12, WINDOW_L)
    for wx in range(x0 + 52, x0 + 100, 10):
        d.rectangle([wx, waterline - 28, wx + 6, waterline - 20], fill=WINDOW, outline=OUT)
        px(d, wx + 1, waterline - 26, WINDOW_L)

    # --- Proa (bico do iate, a direita) ---
    d.polygon([
        (x0 + 150, waterline),
        (x0 + 168, waterline + 4),
        (x0 + 140, waterline + 18),
    ], fill=HULL, outline=OUT)

    # --- Mastro / antena ---
    mast_x = x0 + 78
    d.rectangle([mast_x, waterline - 72, mast_x + 2, waterline - 40], fill=MAST)
    d.line([mast_x + 1, waterline - 70, mast_x + 20, waterline - 50], fill=MAST)
    d.line([mast_x + 1, waterline - 70, mast_x - 18, waterline - 52], fill=MAST)
    # luz no topo
    d.ellipse([mast_x - 2, waterline - 76, mast_x + 4, waterline - 70], fill=LIGHT, outline=OUT)

    # --- Popa (escada / plataforma) ---
    d.rectangle([x0 + 8, waterline + 8, x0 + 20, waterline + 16], fill=HULL_D, outline=OUT)
    # bandeira na popa
    d.rectangle([x0 + 10, waterline - 8, x0 + 12, waterline + 8], fill=MAST)
    d.rectangle([x0 + 12, waterline - 8, x0 + 22, waterline - 2], fill=(0, 120, 60, 255))  # verde
    d.rectangle([x0 + 12, waterline - 2, x0 + 22, waterline + 2], fill=(255, 220, 40, 255))  # amarelo
    d.rectangle([x0 + 12, waterline + 2, x0 + 22, waterline + 6], fill=(0, 90, 180, 255))   # azul

    # --- Reflexo na agua ---
    for i in range(0, 140, 8):
        px(d, x0 + 30 + i, waterline + 28, (HULL_D[0], HULL_D[1], HULL_D[2], 120))
        px(d, x0 + 34 + i, waterline + 30, (WINDOW[0], WINDOW[1], WINDOW[2], 80))


def gen_ui_logo():
    # Pequeno emblema para UI (opcional).
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.ellipse([8, 4, 56, 28], fill=(255, 210, 60, 255), outline=(150, 100, 20, 255))
    img.save(os.path.join(ASSETS, "ui", "logo.png"))


def gen_menu_bg():
    """Fundo tela cheia do menu / tela de ruas: foto do Daniel pixelizada,
    escurecida e com vinheta para nao atrapalhar a leitura do texto."""
    from PIL import ImageEnhance

    ref = os.path.join(os.path.dirname(__file__), "refs", "heroi_menu.png")
    if not os.path.exists(ref):
        print("refs/heroi_menu.png ausente; pulando menu_bg.")
        return

    W, H = 256, 224
    im = Image.open(ref).convert("RGBA")
    bg = Image.new("RGBA", im.size, (0, 0, 0, 255))
    bg.paste(im, (0, 0), im)
    im = bg.convert("RGB")

    bbox = im.getbbox()
    if bbox:
        pad = 8
        l, t, r, b = bbox
        im = im.crop((max(0, l - pad), max(0, t - pad),
                      min(im.width, r + pad), min(im.height, b + pad)))

    iw, ih = im.size
    scale = max(W / iw, H / ih)
    nw, nh = int(iw * scale), int(ih * scale)
    im = im.resize((nw, nh), Image.BILINEAR)
    left, top = (nw - W) // 2, (nh - H) // 2
    im = im.crop((left, top, left + W, top + H))

    # Pixelizar
    pixel_w = 48
    pixel_h = max(1, int(pixel_w * H / W))
    im = im.resize((pixel_w, pixel_h), Image.BILINEAR).resize((W, H), Image.NEAREST)

    im = ImageEnhance.Brightness(im).enhance(0.45)
    im = ImageEnhance.Contrast(im).enhance(0.70)
    im = ImageEnhance.Color(im).enhance(0.75)

    pix = im.load()
    for y in range(H):
        for x in range(W):
            edge_y = min(y, H - 1 - y) / (H * 0.35)
            edge_y = max(0.0, min(1.0, edge_y))
            f = 0.55 + 0.45 * edge_y
            r, g, b = pix[x, y]
            pix[x, y] = (int(r * f), int(g * f), int(b * f))

    out = os.path.join(ASSETS, "textures", "menu_bg.png")
    im.convert("RGBA").save(out)
    print("menu_bg.png gerado:", out)


def main():
    ensure_dirs()
    gen_daniel()
    gen_coin()
    gen_walker_enemy()
    gen_flyer_enemy()
    gen_fast_enemy()
    gen_tank_enemy()
    gen_boss()
    gen_castle()
    gen_tileset()
    for i in range(5):
        gen_background(i)
    gen_ui_logo()
    gen_menu_bg()
    print("Assets gerados em:", ASSETS)


if __name__ == "__main__":
    main()
