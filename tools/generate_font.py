#!/usr/bin/env python3
# =============================================================================
# generate_font.py - Gera BMFont pixelado (AngelCode) para texto nitido em 256x224.
# -----------------------------------------------------------------------------
# rasteriza Pixelon sem anti-alias (threshold), com contorno 1px para legibilidade
# sobre fundos ocupados. Gera assets/fonts/pixel.fnt + pixel.png.
# =============================================================================

import os
from PIL import Image, ImageDraw, ImageFont, ImageFilter

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
OUT_DIR = os.path.join(ROOT, "assets", "fonts")
TTF_CANDIDATES = [
    os.path.join(OUT_DIR, "pixelon.ttf"),
    "/usr/share/fonts/pixelon.regular.ttf",
]

# Tamanho nativo do glifo (px). Menus usam 1x; titulos/HUD usam 2x.
FONT_SIZE = 8
PAD = 1  # padding no atlas
OUTLINE = 1  # contorno preto 1px
ATLAS_W = 512
ATLAS_H = 256


def charset():
    chars = [chr(c) for c in range(32, 127)]  # ASCII imprimivel
    extras = (
        "ÁÀÂÃÄÉÈÊËÍÌÎÏÓÒÔÕÖÚÙÛÜÇÑ"
        "áàâãäéèêëíìîïóòôõöúùûüçñ"
        "ºª°…—–“”‘’¡¿"
    )
    for ch in extras:
        if ch not in chars:
            chars.append(ch)
    return chars


def find_ttf():
    for p in TTF_CANDIDATES:
        if os.path.exists(p):
            return p
    raise SystemExit("Pixelon TTF nao encontrado. Coloque em assets/fonts/pixelon.ttf")


def render_glyph(font, ch):
    """Renderiza glifo branco + contorno preto, sem AA (binario)."""
    # Canvas generoso; corta depois.
    canvas = Image.new("L", (48, 48), 0)
    d = ImageDraw.Draw(canvas)
    d.text((8, 8), ch, font=font, fill=255)
    # Limiar binario (remove AA do FreeType).
    bw = canvas.point(lambda p: 255 if p >= 128 else 0)

    # Contorno: dilata mascara e subtrai o preenchimento.
    if OUTLINE > 0:
        mask = bw.filter(ImageFilter.MaxFilter(OUTLINE * 2 + 1))
        rgba = Image.new("RGBA", bw.size, (0, 0, 0, 0))
        # contorno preto
        outline = Image.new("RGBA", bw.size, (0, 0, 0, 0))
        outline.putalpha(mask)
        black = Image.new("RGBA", bw.size, (0, 0, 0, 255))
        outline = Image.composite(black, Image.new("RGBA", bw.size, (0, 0, 0, 0)), mask)
        # fill branco
        fill = Image.new("RGBA", bw.size, (0, 0, 0, 0))
        white = Image.new("RGBA", bw.size, (255, 255, 255, 255))
        fill = Image.composite(white, fill, bw)
        rgba = Image.alpha_composite(outline, fill)
    else:
        rgba = Image.new("RGBA", bw.size, (0, 0, 0, 0))
        white = Image.new("RGBA", bw.size, (255, 255, 255, 255))
        rgba = Image.composite(white, rgba, bw)

    bbox = rgba.getbbox()
    if bbox is None:
        # espaco / glifo vazio
        advance = max(3, font.getlength(ch) if hasattr(font, "getlength") else 4)
        return Image.new("RGBA", (1, 1), (0, 0, 0, 0)), 0, 0, int(advance) + OUTLINE

    cropped = rgba.crop(bbox)
    # offsets relativos ao ponto de desenho (8,8) usado acima
    xoff = bbox[0] - 8
    yoff = bbox[1] - 8
    advance = int(round(font.getlength(ch))) + OUTLINE
    return cropped, xoff, yoff, max(advance, cropped.size[0] + 1)


def pack_and_write(ttf_path):
    os.makedirs(OUT_DIR, exist_ok=True)
    # Copia TTF para o projeto se ainda nao estiver la.
    local_ttf = os.path.join(OUT_DIR, "pixelon.ttf")
    if ttf_path != local_ttf and not os.path.exists(local_ttf):
        import shutil
        shutil.copy2(ttf_path, local_ttf)
        print("Copiado TTF para", local_ttf)

    font = ImageFont.truetype(ttf_path, FONT_SIZE)
    # métricas aproximadas
    ascent, descent = font.getmetrics()
    line_height = ascent + descent + OUTLINE * 2 + 2
    base = ascent + OUTLINE

    glyphs = []
    for ch in charset():
        img, xoff, yoff, xadv = render_glyph(font, ch)
        glyphs.append((ch, img, xoff, yoff, xadv))

    atlas = Image.new("RGBA", (ATLAS_W, ATLAS_H), (0, 0, 0, 0))
    x = PAD
    y = PAD
    row_h = 0
    packed = []
    for ch, img, xoff, yoff, xadv in glyphs:
        gw, gh = img.size
        if x + gw + PAD > ATLAS_W:
            x = PAD
            y += row_h + PAD
            row_h = 0
        if y + gh + PAD > ATLAS_H:
            raise SystemExit("Atlas pequeno demais para a fonte.")
        atlas.paste(img, (x, y), img)
        packed.append((ch, x, y, gw, gh, xoff, yoff, xadv))
        x += gw + PAD
        row_h = max(row_h, gh)

    png_name = "pixel.png"
    fnt_path = os.path.join(OUT_DIR, "pixel.fnt")
    png_path = os.path.join(OUT_DIR, png_name)
    atlas.save(png_path, optimize=True)

    lines = []
    lines.append(
        'info face="Pixelon" size=%d bold=0 italic=0 charset="" unicode=1 '
        "stretchH=100 smooth=0 aa=0 padding=%d,%d,%d,%d spacing=1,1"
        % (FONT_SIZE, PAD, PAD, PAD, PAD)
    )
    lines.append(
        "common lineHeight=%d base=%d scaleW=%d scaleH=%d pages=1 packed=0"
        % (line_height, base, ATLAS_W, ATLAS_H)
    )
    lines.append('page id=0 file="%s"' % png_name)
    lines.append("chars count=%d" % len(packed))
    for ch, gx, gy, gw, gh, xoff, yoff, xadv in packed:
        lines.append(
            "char id=%d x=%d y=%d width=%d height=%d xoffset=%d yoffset=%d "
            "xadvance=%d page=0 chnl=15"
            % (ord(ch), gx, gy, gw, gh, xoff, yoff, xadv)
        )
    lines.append("kernings count=0")

    with open(fnt_path, "w", encoding="utf-8") as f:
        f.write("\n".join(lines) + "\n")

    print("Fonte gerada:", fnt_path, "+", png_path)
    print("Glifos:", len(packed), "lineHeight=", line_height)


def main():
    pack_and_write(find_ttf())


if __name__ == "__main__":
    main()
