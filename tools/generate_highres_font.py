"""Gera o atlas BMFont suavizado usado apenas pelo trailer e pelas endings."""

import os
from PIL import Image, ImageDraw, ImageFont

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
OUT_DIR = os.path.join(ROOT, "assets", "fonts")
TTF = os.path.join(OUT_DIR, "pixelon.ttf")
FONT_SIZE = 64
PAD = 4
OUTLINE = 3
ATLAS_W = 2048
ATLAS_H = 1024
BASE_NAME = "pixelon-hi"


def charset():
    chars = [chr(c) for c in range(32, 127)]
    extras = (
        "ÁÀÂÃÄÉÈÊËÍÌÎÏÓÒÔÕÖÚÙÛÜÇÑ"
        "áàâãäéèêëíìîïóòôõöúùûüçñ"
        "ºª°…—–“”‘’¡¿"
    )
    return chars + [ch for ch in extras if ch not in chars]


def glyph(font, char):
    origin = FONT_SIZE // 2
    canvas = Image.new("RGBA", (FONT_SIZE * 4, FONT_SIZE * 4), (0, 0, 0, 0))
    draw = ImageDraw.Draw(canvas)
    draw.text((origin, origin), char, font=font, fill=(255, 255, 255, 255),
              stroke_width=OUTLINE, stroke_fill=(0, 0, 0, 255))
    bbox = canvas.getbbox()
    advance = int(round(font.getlength(char))) + OUTLINE
    if bbox is None:
        return Image.new("RGBA", (1, 1), (0, 0, 0, 0)), 0, 0, max(4, advance)
    image = canvas.crop(bbox)
    return image, bbox[0] - origin, bbox[1] - origin, max(advance, image.width + PAD)


def main():
    if not os.path.exists(TTF):
        raise SystemExit(f"Fonte ausente: {TTF}")

    os.makedirs(OUT_DIR, exist_ok=True)
    font = ImageFont.truetype(TTF, FONT_SIZE)
    ascent, descent = font.getmetrics()
    line_height = ascent + descent + OUTLINE * 2 + PAD
    base = ascent + OUTLINE

    atlas = Image.new("RGBA", (ATLAS_W, ATLAS_H), (0, 0, 0, 0))
    x = PAD
    y = PAD
    row_height = 0
    packed = []
    for char in charset():
        image, xoffset, yoffset, advance = glyph(font, char)
        if x + image.width + PAD > ATLAS_W:
            x = PAD
            y += row_height + PAD
            row_height = 0
        if y + image.height + PAD > ATLAS_H:
            raise SystemExit("Atlas insuficiente para a fonte de alta resolucao")
        atlas.paste(image, (x, y), image)
        packed.append((char, x, y, image.width, image.height, xoffset, yoffset, advance))
        x += image.width + PAD
        row_height = max(row_height, image.height)

    png_name = BASE_NAME + ".png"
    fnt_path = os.path.join(OUT_DIR, BASE_NAME + ".fnt")
    atlas.save(os.path.join(OUT_DIR, png_name), optimize=True)

    lines = [
        f'info face="Pixelon High" size={FONT_SIZE} bold=0 italic=0 charset="" '
        f"unicode=1 stretchH=100 smooth=1 aa=1 padding={PAD},{PAD},{PAD},{PAD} spacing=1,1",
        f"common lineHeight={line_height} base={base} scaleW={ATLAS_W} scaleH={ATLAS_H} pages=1 packed=0",
        f'page id=0 file="{png_name}"',
        f"chars count={len(packed)}",
    ]
    for char, gx, gy, gw, gh, xoffset, yoffset, advance in packed:
        lines.append(
            f"char id={ord(char)} x={gx} y={gy} width={gw} height={gh} "
            f"xoffset={xoffset} yoffset={yoffset} xadvance={advance} page=0 chnl=15"
        )
    lines.append("kernings count=0")
    with open(fnt_path, "w", encoding="utf-8") as output:
        output.write("\n".join(lines) + "\n")
    print(f"Fonte gerada: {fnt_path}")


if __name__ == "__main__":
    main()
