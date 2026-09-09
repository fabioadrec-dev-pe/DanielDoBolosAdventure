from PIL import Image, ImageDraw, ImageFont, ImageFilter, ImageOps
import numpy as np
import os

ROOT = os.path.dirname(os.path.abspath(__file__))
OUT_DIR = os.path.join(ROOT, 'labels_geradas')
os.makedirs(OUT_DIR, exist_ok=True)

CARTUCHO_ORIGINAL = '/home/fabio/Downloads/Imagem do Codex 4 de set. de 2026, 23_13_48.png'
CARTUCHO_SFC_CLEAN = '/tmp/sfc_cart_clean2.png'
LABEL_BASE = '/tmp/label_crop_v1.png'

LABEL_W, LABEL_H = 2048, 900

FONT_TITULO = "/usr/share/fonts/truetype/dejavu/DejaVuSans-BoldOblique.ttf"
FONT_SLOGAN = "/usr/share/fonts/truetype/dejavu/DejaVuSans-BoldOblique.ttf"
FONT_LOGO = "/usr/share/fonts/truetype/dejavu/DejaVuSansCondensed-Bold.ttf"
FONT_JP = "/usr/share/fonts/opentype/noto/NotoSansCJK-Bold.ttc"

def load_font(path, size):
    try:
        return ImageFont.truetype(path, size)
    except Exception as e:
        print(f"Fonte não encontrada: {path}, usando default. Erro: {e}")
        return ImageFont.load_default()

def find_homography(src, dst):
    A = []
    b = []
    for (x, y), (xp, yp) in zip(src, dst):
        A.append([x, y, 1, 0, 0, 0, -x*xp, -y*xp])
        A.append([0, 0, 0, x, y, 1, -x*yp, -y*yp])
        b.extend([xp, yp])
    A = np.array(A)
    b = np.array(b)
    h = np.linalg.lstsq(A, b, rcond=None)[0]
    return np.append(h, 1).reshape(3, 3)

def get_text_size(text, font):
    temp = Image.new('RGBA', (1, 1))
    d = ImageDraw.Draw(temp)
    bbox = d.textbbox((0, 0), text, font=font)
    return bbox[2] - bbox[0], bbox[3] - bbox[1]

def draw_gradient_text(text, font_path, size, colors, stroke_color=(0, 0, 0), stroke_width=4, shadow=True):
    font = load_font(font_path, size)
    bbox = ImageDraw.Draw(Image.new('RGBA', (1, 1))).textbbox((0, 0), text, font=font, stroke_width=stroke_width)
    w, h = bbox[2] - bbox[0], bbox[3] - bbox[1]
    padding = stroke_width + 20
    W, H = w + padding * 2, h + padding * 2
    img = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    mask = Image.new('L', (W, H), 0)
    md = ImageDraw.Draw(mask)
    md.text((padding, padding), text, font=font, fill=255, stroke_width=stroke_width, stroke_fill=255)
    gradient = Image.new('RGBA', (W, H))
    gd = ImageDraw.Draw(gradient)
    for y in range(H):
        ratio = y / H
        r = int(colors[0][0] + (colors[1][0] - colors[0][0]) * ratio)
        g = int(colors[0][1] + (colors[1][1] - colors[0][1]) * ratio)
        b = int(colors[0][2] + (colors[1][2] - colors[0][2]) * ratio)
        gd.line([(0, y), (W, y)], fill=(r, g, b, 255))
    result = Image.composite(gradient, Image.new('RGBA', (W, H), (0, 0, 0, 0)), mask)
    if shadow:
        shadow_mask = mask.filter(ImageFilter.GaussianBlur(radius=8))
        shadow_img = Image.new('RGBA', (W, H), (0, 0, 0, 180))
        final = Image.new('RGBA', (W, H), (0, 0, 0, 0))
        final.paste(shadow_img, (5, 5), shadow_mask)
        final = Image.alpha_composite(final, result)
        return final
    return result

def draw_text_with_shadow(text, font_path, size, fill=(255, 255, 255), stroke_color=None, stroke_width=0, shadow_color=(0, 0, 0, 160)):
    font = load_font(font_path, size)
    bbox = ImageDraw.Draw(Image.new('RGBA', (1, 1))).textbbox((0, 0), text, font=font, stroke_width=stroke_width)
    w, h = bbox[2] - bbox[0], bbox[3] - bbox[1]
    padding = stroke_width + 15
    W, H = w + padding * 2, h + padding * 2
    img = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.text((padding + 3, padding + 3), text, font=font, fill=shadow_color, stroke_width=stroke_width, stroke_fill=shadow_color)
    stroke_fill = stroke_color + (255,) if stroke_color else None
    d.text((padding, padding), text, font=font, fill=fill + (255,), stroke_width=stroke_width, stroke_fill=stroke_fill)
    return img

def draw_nintendo_logo(width=200, height=50):
    img = Image.new('RGBA', (width, height), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.ellipse([4, 4, width - 4, height - 4], fill=(220, 20, 20, 255), outline=(255, 255, 255, 255), width=2)
    font = load_font(FONT_LOGO, int(height * 0.55))
    text = "Nintendo"
    tw, th = get_text_size(text, font)
    d.text(((width - tw) // 2, (height - th) // 2 - 2), text, font=font, fill=(255, 255, 255, 255))
    return img

def draw_snes_logo(width=420, height=80):
    img = Image.new('RGBA', (width, height), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    font = load_font(FONT_LOGO, 46)
    txt1 = "SUPER NINTENDO"
    d.text((12, 2), txt1, font=font, fill=(180, 0, 0, 255))
    d.text((10, 0), txt1, font=font, fill=(255, 255, 255, 255))
    font2 = load_font(FONT_LOGO, 20)
    txt2 = "ENTERTAINMENT SYSTEM"
    d.text((12, 52), txt2, font=font2, fill=(255, 255, 255, 255))
    return img

def draw_16bit_badge(width=220, height=70):
    img = Image.new('RGBA', (width, height), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    font = load_font(FONT_LOGO, 38)
    txt = "16-BIT"
    tw, th = get_text_size(txt, font)
    d.text((10, (height - th) // 2), txt, font=font, fill=(255, 255, 255, 255))
    colors = [(220, 20, 60), (255, 215, 0), (50, 205, 50), (30, 144, 255)]
    cy = height // 2
    cx = tw + 30
    for color in colors:
        d.ellipse([cx - 12, cy - 12, cx + 12, cy + 12], fill=color + (255,), outline=(0, 0, 0, 255), width=2)
        cx += 30
    return img

def draw_sfc_logo(width=360, height=80):
    img = Image.new('RGBA', (width, height), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    font = load_font(FONT_LOGO, 48)
    txt = "SUPER FAMICOM"
    d.text((3, 3), txt, font=font, fill=(0, 0, 0, 160))
    d.text((0, 0), txt, font=font, fill=(220, 20, 20, 255))
    return img

def draw_sfc_balls(size=90):
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    colors = [(220, 20, 60), (255, 215, 0), (50, 205, 50), (30, 144, 255)]
    positions = [(20, 25), (50, 15), (65, 45), (35, 60)]
    for color, pos in zip(colors, positions):
        d.ellipse([pos[0]-14, pos[1]-14, pos[0]+14, pos[1]+14], fill=color+(255,), outline=(0,0,0,255), width=2)
    return img

def make_label_snes_improved():
    base = Image.open(LABEL_BASE).convert('RGBA')
    overlay = Image.new('RGBA', (LABEL_W, LABEL_H), (0, 0, 0, 0))
    d = ImageDraw.Draw(overlay)
    
    # Faixa superior azul claro (céu de dia) para cobrir título antigo
    band_h = 580
    for y in range(band_h):
        ratio = y / band_h
        r = int(135 - 20 * ratio)
        g = int(206 - 30 * ratio)
        b = int(250 - 20 * ratio)
        d.line([(0, y), (LABEL_W, y)], fill=(r, g, b, 255))
    # Fade suave para a arte inferior
    fade = 60
    for y in range(band_h, band_h + fade):
        alpha = int(255 * (1 - (y - band_h) / fade))
        d.line([(0, y), (LABEL_W, y)], fill=(115, 176, 230, alpha))
    
    # Título principal
    title1 = "DANIEL DO BOLO'S"
    title2 = "ADVENTURE"
    t1 = draw_gradient_text(title1, FONT_TITULO, 48, [(255, 215, 0), (255, 100, 0)], stroke_width=5)
    t2 = draw_gradient_text(title2, FONT_TITULO, 60, [(255, 215, 0), (255, 50, 0)], stroke_width=6)
    tx1 = (LABEL_W - t1.width) // 2
    tx2 = (LABEL_W - t2.width) // 2
    ty1 = 160
    ty2 = ty1 + t1.height - 18
    overlay.paste(t1, (tx1, ty1), t1)
    overlay.paste(t2, (tx2, ty2), t2)
    
    # Slogan canto superior direito
    s1 = "DE BRASÍLIA TEIMOSA"
    s2 = "PARA NORONHA!"
    s1_img = draw_text_with_shadow(s1, FONT_SLOGAN, 18, fill=(0, 0, 80), stroke_color=(255, 255, 255), stroke_width=2)
    s2_img = draw_text_with_shadow(s2, FONT_SLOGAN, 18, fill=(0, 0, 80), stroke_color=(255, 255, 255), stroke_width=2)
    overlay.paste(s1_img, (LABEL_W - s1_img.width - 30, 170), s1_img)
    overlay.paste(s2_img, (LABEL_W - s2_img.width - 30, 200), s2_img)
    
    label = Image.alpha_composite(base, overlay)
    
    # Faixa inferior azul claro para logos
    lo = Image.new('RGBA', (LABEL_W, LABEL_H), (0, 0, 0, 0))
    dlo = ImageDraw.Draw(lo)
    bottom_h = 110
    for y in range(LABEL_H - bottom_h, LABEL_H):
        dlo.line([(0, y), (LABEL_W, y)], fill=(115, 176, 230, 255))
    for y in range(LABEL_H - bottom_h - 30, LABEL_H - bottom_h):
        alpha = int(255 * (1 - (LABEL_H - bottom_h - y) / 30))
        dlo.line([(0, y), (LABEL_W, y)], fill=(115, 176, 230, alpha))
    
    # Logos
    nintendo = draw_nintendo_logo(170, 45)
    lo.paste(nintendo, (30, LABEL_H - 75), nintendo)
    snes = draw_snes_logo(400, 75)
    lo.paste(snes, (220, LABEL_H - 80), snes)
    bit16 = draw_16bit_badge(220, 60)
    lo.paste(bit16, (LABEL_W - bit16.width - 30, LABEL_H - 78), bit16)
    label = Image.alpha_composite(label, lo)
    return label

def make_label_sfc():
    base = Image.open(LABEL_BASE).convert('RGBA')
    overlay = Image.new('RGBA', (LABEL_W, LABEL_H), (0, 0, 0, 0))
    d = ImageDraw.Draw(overlay)
    
    # Faixa superior azul claro (céu de dia) para cobrir título antigo
    band_h = 580
    for y in range(band_h):
        ratio = y / band_h
        r = int(135 - 20 * ratio)
        g = int(206 - 30 * ratio)
        b = int(250 - 20 * ratio)
        d.line([(0, y), (LABEL_W, y)], fill=(r, g, b, 255))
    # Fade
    fade = 60
    for y in range(band_h, band_h + fade):
        alpha = int(255 * (1 - (y - band_h) / fade))
        d.line([(0, y), (LABEL_W, y)], fill=(115, 176, 230, alpha))
    
    # Título japonês
    t1 = "ダニエル・ド・ボロ"
    t2 = "アドベンチャー"
    t1_img = draw_gradient_text(t1, FONT_JP, 40, [(255, 223, 0), (255, 140, 0)], stroke_width=4)
    t2_img = draw_gradient_text(t2, FONT_JP, 50, [(255, 223, 0), (255, 80, 0)], stroke_width=5)
    tx1 = (LABEL_W - t1_img.width) // 2
    tx2 = (LABEL_W - t2_img.width) // 2
    ty1 = 160
    ty2 = ty1 + t1_img.height - 18
    overlay.paste(t1_img, (tx1, ty1), t1_img)
    overlay.paste(t2_img, (tx2, ty2), t2_img)
    
    # Slogan japonês
    s1 = "ブラジリア・テイモーザ"
    s2 = "ノローニャへ"
    s1_img = draw_text_with_shadow(s1, FONT_JP, 14, fill=(0, 0, 80), stroke_color=(255, 255, 255), stroke_width=2)
    s2_img = draw_text_with_shadow(s2, FONT_JP, 16, fill=(0, 0, 80), stroke_color=(255, 255, 255), stroke_width=2)
    overlay.paste(s1_img, (LABEL_W - s1_img.width - 25, 170), s1_img)
    overlay.paste(s2_img, (LABEL_W - s2_img.width - 25, 200), s2_img)
    
    label = Image.alpha_composite(base, overlay)
    
    # Faixa inferior azul claro
    lo = Image.new('RGBA', (LABEL_W, LABEL_H), (0, 0, 0, 0))
    dlo = ImageDraw.Draw(lo)
    bottom_h = 110
    for y in range(LABEL_H - bottom_h, LABEL_H):
        dlo.line([(0, y), (LABEL_W, y)], fill=(115, 176, 230, 255))
    for y in range(LABEL_H - bottom_h - 30, LABEL_H - bottom_h):
        alpha = int(255 * (1 - (LABEL_H - bottom_h - y) / 30))
        dlo.line([(0, y), (LABEL_W, y)], fill=(115, 176, 230, alpha))
    
    # Logos SFC
    balls = draw_sfc_balls(90)
    lo.paste(balls, (30, LABEL_H - 95), balls)
    sfc = draw_sfc_logo(330, 70)
    lo.paste(sfc, (130, LABEL_H - 80), sfc)
    code_font = load_font(FONT_LOGO, 22)
    code = "SHVC-DDBJ-JPN"
    tw, th = get_text_size(code, code_font)
    cd = ImageDraw.Draw(lo)
    cd.text((LABEL_W - tw - 30, LABEL_H - 70), code, font=code_font, fill=(0, 0, 0, 255))
    cd.text((LABEL_W - tw - 32, LABEL_H - 72), code, font=code_font, fill=(255, 255, 255, 180))
    label = Image.alpha_composite(label, lo)
    return label

def warp_label_to_cartucho(label, src, cart_size):
    label_w, label_h = label.size
    dst = np.array([[0, 0], [label_w, 0], [label_w, label_h], [0, label_h]], dtype=np.float32)
    H = find_homography(src, dst)
    H_inv = np.linalg.inv(H)
    coeffs = H_inv.flatten()[:8].tolist()
    return label.transform(cart_size, Image.Transform.PERSPECTIVE, data=coeffs, resample=Image.Resampling.BICUBIC)

def main():
    src = np.array([[340, 170], [1200, 170], [1200, 535], [340, 535]], dtype=np.float32)
    
    # SNES melhorado
    label_snes = make_label_snes_improved()
    label_snes.save(os.path.join(OUT_DIR, 'label_snes_melhorada.png'))
    cart = Image.open(CARTUCHO_ORIGINAL).convert('RGBA')
    warped = warp_label_to_cartucho(label_snes, src, cart.size)
    result = Image.alpha_composite(cart, warped)
    result.save(os.path.join(OUT_DIR, 'cartucho_snes_melhorado.png'))
    print('SNES salvo')
    
    # SFC
    label_sfc = make_label_sfc()
    label_sfc.save(os.path.join(OUT_DIR, 'label_sfc_japonesa.png'))
    cart_sfc = Image.open(CARTUCHO_SFC_CLEAN).convert('RGBA')
    x1, y1, x2, y2 = 110, 1180, 2900, 2160
    target_w = x2 - x1
    target_h = y2 - y1
    label_sfc_resized = label_sfc.resize((target_w, target_h), Image.Resampling.LANCZOS)
    mask = Image.new('L', (target_w, target_h), 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, target_w, target_h], radius=40, fill=255)
    label_sfc_masked = Image.new('RGBA', (target_w, target_h), (0,0,0,0))
    label_sfc_masked.paste(label_sfc_resized, (0,0), mask)
    cart_sfc.paste(label_sfc_masked, (x1, y1), mask)
    cart_sfc.save(os.path.join(OUT_DIR, 'cartucho_sfc_japones.png'))
    print('SFC salvo')

if __name__ == '__main__':
    main()
