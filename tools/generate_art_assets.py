from __future__ import annotations

import math
import random
import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter, ImageFont


ROOT = Path(__file__).resolve().parents[1]
DRAWABLE_NODPI = ROOT / "app" / "src" / "main" / "res" / "drawable-nodpi"
DRAWABLE = ROOT / "app" / "src" / "main" / "res" / "drawable"


def hex_to_rgb(value: str) -> tuple[int, int, int]:
    value = value.strip("#")
    return tuple(int(value[index : index + 2], 16) for index in (0, 2, 4))


def lerp(a: int, b: int, t: float) -> int:
    return round(a + (b - a) * t)


def gradient(size: tuple[int, int], top: str, middle: str, bottom: str) -> Image.Image:
    width, height = size
    top_rgb = hex_to_rgb(top)
    middle_rgb = hex_to_rgb(middle)
    bottom_rgb = hex_to_rgb(bottom)
    image = Image.new("RGB", size)
    draw = ImageDraw.Draw(image)
    for y in range(height):
        t = y / max(1, height - 1)
        if t < 0.48:
            lt = t / 0.48
            color = tuple(lerp(top_rgb[i], middle_rgb[i], lt) for i in range(3))
        else:
            lt = (t - 0.48) / 0.52
            color = tuple(lerp(middle_rgb[i], bottom_rgb[i], lt) for i in range(3))
        draw.line((0, y, width, y), fill=color)
    return image


def add_noise(image: Image.Image, amount: int, seed: int) -> Image.Image:
    random.seed(seed)
    pixels = image.load()
    width, height = image.size
    for y in range(height):
        for x in range(width):
            delta = random.randint(-amount, amount)
            r, g, b = pixels[x, y][:3]
            pixels[x, y] = (
                max(0, min(255, r + delta)),
                max(0, min(255, g + delta)),
                max(0, min(255, b + delta)),
            )
    return image


def add_vignette(image: Image.Image, strength: float = 0.58) -> Image.Image:
    width, height = image.size
    overlay = Image.new("RGBA", image.size, (0, 0, 0, 0))
    draw = ImageDraw.Draw(overlay)
    cx, cy = width / 2, height / 2
    max_distance = math.hypot(cx, cy)
    for y in range(height):
        for x in range(width):
            distance = math.hypot(x - cx, y - cy) / max_distance
            alpha = int(255 * max(0, distance - 0.26) * strength)
            draw.point((x, y), fill=(0, 0, 0, min(190, alpha)))
    return Image.alpha_composite(image.convert("RGBA"), overlay)


def rounded_mask(size: tuple[int, int], radius: int) -> Image.Image:
    mask = Image.new("L", size, 0)
    draw = ImageDraw.Draw(mask)
    draw.rounded_rectangle((0, 0, size[0] - 1, size[1] - 1), radius=radius, fill=255)
    return mask


def wood_grain(size: tuple[int, int], base: Image.Image, seed: int, horizontal: bool = True) -> Image.Image:
    random.seed(seed)
    width, height = size
    draw = ImageDraw.Draw(base)
    line_count = 75 if horizontal else 55
    for i in range(line_count):
        if horizontal:
            y = random.randint(0, height)
            points = []
            for x in range(-40, width + 41, 34):
                wave = math.sin((x + i * 27) / 62.0) * random.uniform(4, 17)
                points.append((x, y + wave + random.uniform(-5, 5)))
        else:
            x = random.randint(0, width)
            points = []
            for y in range(-40, height + 41, 34):
                wave = math.sin((y + i * 21) / 58.0) * random.uniform(4, 15)
                points.append((x + wave + random.uniform(-4, 4), y))
        color = random.choice([(10, 6, 4, 58), (115, 72, 35, 35), (212, 160, 77, 16)])
        draw.line(points, fill=color, width=random.choice([1, 1, 2, 3]), joint="curve")
    return base.filter(ImageFilter.GaussianBlur(0.35))


def create_background(ai_source: Path | None) -> None:
    output = DRAWABLE_NODPI / "noble_table_bg.png"
    if ai_source and ai_source.exists():
        source = Image.open(ai_source).convert("RGB")
        target_ratio = 1080 / 1920
        sw, sh = source.size
        if sw / sh > target_ratio:
            new_w = int(sh * target_ratio)
            left = (sw - new_w) // 2
            source = source.crop((left, 0, left + new_w, sh))
        else:
            new_h = int(sw / target_ratio)
            top = max(0, (sh - new_h) // 2)
            source = source.crop((0, top, sw, top + new_h))
        bg = source.resize((1080, 1920), Image.Resampling.LANCZOS)
    else:
        bg = gradient((1080, 1920), "#24170E", "#3A2515", "#15100B")
        bg = wood_grain(bg.size, bg.convert("RGBA"), seed=10)
    bg = add_vignette(bg, 0.64)
    bg.save(output)


def create_panel_assets() -> None:
    wood = gradient((768, 512), "#6A4524", "#3A2515", "#15100B").convert("RGBA")
    wood = wood_grain(wood.size, wood, seed=22)
    wood = add_noise(wood.convert("RGB"), 6, seed=23).convert("RGBA")
    shine = Image.new("RGBA", wood.size, (255, 255, 255, 0))
    draw = ImageDraw.Draw(shine)
    draw.rounded_rectangle((18, 16, wood.width - 18, 142), radius=28, fill=(255, 235, 175, 22))
    wood = Image.alpha_composite(wood, shine)
    wood.save(DRAWABLE_NODPI / "noble_wood_panel.png")

    parchment = gradient((768, 512), "#F3E3BD", "#E6D2A8", "#CBB17E").convert("RGBA")
    parchment = add_noise(parchment.convert("RGB"), 9, seed=31).convert("RGBA")
    draw = ImageDraw.Draw(parchment)
    for _ in range(90):
        x = random.randint(0, parchment.width)
        y = random.randint(0, parchment.height)
        color = random.choice([(80, 45, 18, 18), (255, 255, 230, 18), (153, 112, 55, 18)])
        draw.ellipse((x, y, x + random.randint(1, 5), y + random.randint(1, 4)), fill=color)
    parchment.filter(ImageFilter.GaussianBlur(0.1)).save(DRAWABLE_NODPI / "noble_parchment_panel.png")

    board = gradient((1024, 1024), "#6A4524", "#2D1B10", "#15100B").convert("RGBA")
    board = wood_grain(board.size, board, seed=48)
    draw = ImageDraw.Draw(board)
    for inset, alpha in [(12, 70), (24, 44), (42, 28)]:
        draw.rounded_rectangle((inset, inset, board.width - inset, board.height - inset), radius=52, outline=(209, 166, 75, alpha), width=4)
    board.save(DRAWABLE_NODPI / "noble_board_texture.png")


def create_ice_asset() -> None:
    ice = gradient((512, 512), "#F4FBFF", "#BEEBFF", "#416E7D").convert("RGBA")
    alpha = Image.new("L", ice.size, 112)
    ice.putalpha(alpha)
    draw = ImageDraw.Draw(ice)
    random.seed(70)
    for _ in range(36):
        start = (random.randint(0, ice.width), random.randint(0, ice.height))
        end = (start[0] + random.randint(-160, 160), start[1] + random.randint(-160, 160))
        draw.line((start, end), fill=(244, 251, 255, random.randint(55, 120)), width=random.choice([1, 2, 3]))
    for inset in (8, 18, 32):
        draw.rounded_rectangle((inset, inset, ice.width - inset, ice.height - inset), radius=44, outline=(244, 251, 255, 90), width=5)
    ice.save(DRAWABLE_NODPI / "noble_ice_overlay.png")


def create_launcher_foreground() -> None:
    size = 432
    image = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    center = size // 2
    draw.rounded_rectangle((66, 76, 366, 356), radius=58, fill=(45, 27, 16, 255), outline=(168, 121, 42, 255), width=10)
    draw.rounded_rectangle((96, 106, 336, 326), radius=42, fill=(230, 210, 168, 255), outline=(209, 166, 75, 255), width=8)
    draw.rounded_rectangle((126, 136, 306, 296), radius=30, fill=(209, 166, 75, 255), outline=(255, 226, 154, 255), width=6)
    try:
        font = ImageFont.truetype("C:/Windows/Fonts/georgiab.ttf", 64)
    except Exception:
        font = ImageFont.load_default()
    text = "4096"
    bbox = draw.textbbox((0, 0), text, font=font)
    tx = center - (bbox[2] - bbox[0]) / 2
    ty = center - (bbox[3] - bbox[1]) / 2 - 3
    draw.text((tx + 2, ty + 3), text, fill=(35, 22, 12, 120), font=font)
    draw.text((tx, ty), text, fill=(38, 24, 14, 255), font=font)
    draw.arc((142, 50, 290, 142), start=200, end=340, fill=(209, 166, 75, 255), width=7)
    image.save(DRAWABLE / "ic_launcher_foreground.png")


def main() -> None:
    DRAWABLE_NODPI.mkdir(parents=True, exist_ok=True)
    DRAWABLE.mkdir(parents=True, exist_ok=True)
    ai_source = Path(sys.argv[1]) if len(sys.argv) > 1 else None
    create_background(ai_source)
    create_panel_assets()
    create_ice_asset()
    create_launcher_foreground()


if __name__ == "__main__":
    main()
