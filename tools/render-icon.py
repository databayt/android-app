#!/usr/bin/env python3
"""Render an Apple Icon Composer .icon bundle to a PNG tile.

Usage:
    python3 tools/render-icon.py <name.icon> <ic_tile_name.png> [extra_target.png ...]

Examples:
    python3 tools/render-icon.py notification.icon ic_tile_notifications.png ic_tile_alerts.png
    python3 tools/render-icon.py exam.icon       ic_tile_exams.png

Reads icon.json: fill (linear-gradient or automatic-gradient), layer scale, glyph PNG.
Bakes the orange/blue/etc. base + iOS-style top-light gradient + crisp white glyph
+ neutral drop shadow + soft inner highlight. Writes 1024x1024 PNG to
feature/dashboard/src/main/res/drawable-nodpi/<target>.
"""
from __future__ import annotations
import json
import sys
from pathlib import Path
from PIL import Image, ImageDraw, ImageFilter

REPO = Path(__file__).resolve().parent.parent
DRAWABLE = REPO / 'feature/dashboard/src/main/res/drawable-nodpi'
SIZE = 1024
RADIUS = 220


def parse_color(s: str) -> tuple[int, int, int]:
    return tuple(int(float(c) * 255) for c in s.split(':')[1].split(',')[:3])


def render(icon_dir: Path, targets: list[str]) -> None:
    spec = json.loads((icon_dir / 'icon.json').read_text())

    fill = spec['fill']
    if 'automatic-gradient' in fill:
        top_color = bot_color = parse_color(fill['automatic-gradient'])
        two_stop = False
    else:
        stops = fill['linear-gradient']
        top_color = parse_color(stops[0])
        bot_color = parse_color(stops[-1])
        two_stop = (top_color != bot_color)

    grad = Image.new('RGBA', (1, SIZE))
    for y in range(SIZE):
        t = y / (SIZE - 1)
        if two_stop:
            # interpolate top→bot, then add subtle iOS sheen on top edge
            c = tuple(int(top_color[i] + (bot_color[i] - top_color[i]) * t) for i in range(3))
            if t < 0.15:
                k = 0.18 * (1 - t / 0.15)
                c = tuple(int(c[i] + (255 - c[i]) * k) for i in range(3))
        else:
            if t < 0.5:
                k = 0.28 * (1 - 2 * t)
                c = tuple(int(top_color[i] + (255 - top_color[i]) * k) for i in range(3))
            else:
                k = 0.12 * (2 * t - 1)
                c = tuple(int(top_color[i] * (1 - k)) for i in range(3))
        grad.putpixel((0, y), (*c, 255))
    grad = grad.resize((SIZE, SIZE))

    mask = Image.new('L', (SIZE, SIZE), 0)
    ImageDraw.Draw(mask).rounded_rectangle([(0, 0), (SIZE - 1, SIZE - 1)], radius=RADIUS, fill=255)

    base = Image.new('RGBA', (SIZE, SIZE), (0, 0, 0, 0))
    base.paste(grad, (0, 0), mask)

    layer = spec['groups'][0]['layers'][0]
    scale = layer['position']['scale']
    target = int(SIZE / 2 * scale)

    glyph_src = Image.open(icon_dir / 'Assets' / layer['image-name']).convert('RGBA')
    glyph_src = glyph_src.resize((target, target), Image.LANCZOS)
    a = glyph_src.split()[3]
    a_solid = a.point(lambda p: 255 if p > 12 else int(p * 20))

    white_only = Image.new('RGBA', glyph_src.size, (255, 255, 255, 0))
    white_only.putalpha(a_solid)

    shadow_alpha = a_solid.point(lambda p: int(p * 0.55))
    shadow = Image.new('RGBA', glyph_src.size, (0, 0, 0, 0))
    shadow.putalpha(shadow_alpha)
    shadow = shadow.filter(ImageFilter.GaussianBlur(radius=22))

    gx = (SIZE - target) // 2
    gy = (SIZE - target) // 2

    shadow_canvas = Image.new('RGBA', (SIZE, SIZE), (0, 0, 0, 0))
    shadow_canvas.paste(shadow, (gx, gy + 14), shadow)

    hl_grad = Image.new('L', (1, target))
    for y in range(target):
        hl_grad.putpixel((0, y), max(0, int(180 * (1 - y / target) ** 2)))
    hl_grad = hl_grad.resize((target, target))
    hl_pix = hl_grad.load()
    a_pix = a_solid.load()
    hl_alpha = Image.new('L', (target, target))
    hl_alpha_pix = hl_alpha.load()
    for x in range(target):
        for y in range(target):
            hl_alpha_pix[x, y] = (hl_pix[x, y] * a_pix[x, y]) // 255
    highlight = Image.new('RGBA', (target, target), (255, 255, 255, 0))
    highlight.putalpha(hl_alpha)

    glyph_canvas = Image.new('RGBA', (SIZE, SIZE), (0, 0, 0, 0))
    glyph_canvas.paste(white_only, (gx, gy), white_only)
    glyph_canvas.paste(highlight, (gx, gy), highlight)

    glyph_masked = Image.new('RGBA', (SIZE, SIZE), (0, 0, 0, 0))
    glyph_masked.paste(glyph_canvas, (0, 0), mask)

    composed = Image.alpha_composite(base, shadow_canvas)
    composed = Image.alpha_composite(composed, glyph_masked)

    for t in targets:
        out = DRAWABLE / t
        composed.save(out, optimize=True)
        print(f'  wrote {out.relative_to(REPO)}')

    print(f'rendered {icon_dir.name}: scale={scale} ({target}px / {SIZE}px canvas)')


def main() -> int:
    if len(sys.argv) < 3:
        print(__doc__)
        return 1
    icon_dir = Path(sys.argv[1])
    if not icon_dir.is_absolute():
        icon_dir = REPO / icon_dir
    if not (icon_dir / 'icon.json').exists():
        print(f'error: {icon_dir}/icon.json not found')
        return 1
    targets = sys.argv[2:]
    for t in targets:
        if not t.endswith('.png'):
            print(f'error: target {t!r} must end with .png')
            return 1
    render(icon_dir, targets)
    return 0


if __name__ == '__main__':
    sys.exit(main())
