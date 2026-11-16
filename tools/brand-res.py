#!/usr/bin/env python3
"""Generate a school's Android brand resources from its brand folder.

Usage:
    python3 tools/brand-res.py <brand-dir> [--preview <dir>]

<brand-dir> holds brand.properties (label, label.en, color) and icon.png — a
square app icon: the artwork on a solid ground colour (white or transparent
outside the rounded corners is fine). The ground colour is keyed out, so the
artwork becomes the adaptive-icon foreground over a solid `color` background.

Writes <brand-dir>/res/, which the `school` flavor uses as its res source set
(`-Phogwarts.brand=<brand-dir>`), overriding main:
  drawable-<dpi>/ic_launcher_foreground.png   108dp, artwork inside the 66dp safe circle
  drawable-<dpi>/ic_splash_quill.png          240dp, artwork inside the 160dp visible circle
  values/colors.xml                           balqalam_ground = color
  values/strings.xml, values-ar/strings.xml   app_name = label.en / label
--preview writes circle + squircle launcher mocks and the splash to look at.

School brands live outside the public repo (real client names and marks).
"""
from __future__ import annotations

import sys
from pathlib import Path
from xml.sax.saxutils import escape

from PIL import Image, ImageChops, ImageDraw

DENSITIES = {"mdpi": 1.0, "hdpi": 1.5, "xhdpi": 2.0, "xxhdpi": 3.0, "xxxhdpi": 4.0}
LAUNCHER_DP, LAUNCHER_SAFE_R_DP = 108, 31.0  # safe circle is r=33dp; keep a margin
SPLASH_DP, SPLASH_SAFE_R_DP = 240, 72.0  # visible circle is r=80dp


def read_props(path: Path) -> dict[str, str]:
    props: dict[str, str] = {}
    for line in path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        k, v = line.split("=", 1)
        props[k.strip()] = v.strip()
    return props


def hex_rgb(s: str) -> tuple[int, int, int]:
    s = s.lstrip("#")
    return int(s[0:2], 16), int(s[2:4], 16), int(s[4:6], 16)


def extract_artwork(icon: Image.Image, ground: tuple[int, int, int]) -> Image.Image:
    """Alpha = how far each pixel is from the ground colour; white/transparent corners drop out."""
    rgba = icon.convert("RGBA")
    w, h = rgba.size
    src = rgba.load()
    out = Image.new("RGBA", (w, h))
    dst = out.load()
    gr, gg, gb = ground
    for y in range(h):
        for x in range(w):
            r, g, b, a = src[x, y]
            if a < 8 or (r > 200 and g > 200 and b > 200):  # corners / outside the tile
                dst[x, y] = (0, 0, 0, 0)
                continue
            if b > r - 30 and (gr < gb):  # ground→white edge blends (artwork is warmer than a cool ground)
                dst[x, y] = (0, 0, 0, 0)
                continue
            dist = ((r - gr) ** 2 + (g - gg) ** 2 + (b - gb) ** 2) ** 0.5
            alpha = max(0.0, min(1.0, (dist - 40) / 140))
            if alpha <= 0:
                dst[x, y] = (0, 0, 0, 0)
                continue
            # un-mix the ground so edges keep the artwork's own colour
            ur = int(max(0, min(255, (r - (1 - alpha) * gr) / alpha)))
            ug = int(max(0, min(255, (g - (1 - alpha) * gg) / alpha)))
            ub = int(max(0, min(255, (b - (1 - alpha) * gb) / alpha)))
            dst[x, y] = (ur, ug, ub, int(alpha * 255))
    bbox = out.getbbox()
    if not bbox:
        sys.exit("brand-res: no artwork found after keying out the ground colour")
    return out.crop(bbox)


def max_radius(art: Image.Image) -> float:
    """Largest distance of a visible pixel from the artwork's centre."""
    w, h = art.size
    cx, cy = w / 2, h / 2
    alpha = art.getchannel("A").load()
    r = 0.0
    for y in range(0, h, 2):
        for x in range(0, w, 2):
            if alpha[x, y] > 32:
                r = max(r, ((x - cx) ** 2 + (y - cy) ** 2) ** 0.5)
    return r


def place(art: Image.Image, art_r: float, canvas_px: int, safe_r_px: float) -> Image.Image:
    scale = safe_r_px / art_r
    w, h = max(1, round(art.width * scale)), max(1, round(art.height * scale))
    scaled = art.resize((w, h), Image.LANCZOS)
    canvas = Image.new("RGBA", (canvas_px, canvas_px), (0, 0, 0, 0))
    canvas.alpha_composite(scaled, ((canvas_px - w) // 2, (canvas_px - h) // 2))
    return canvas


def mask_preview(fg: Image.Image, ground: tuple[int, int, int], squircle: bool) -> Image.Image:
    size = fg.width
    tile = Image.new("RGBA", (size, size), ground + (255,))
    tile.alpha_composite(fg)
    # launchers show the inner 72dp of 108dp
    inset = round(size * 18 / 108)
    tile = tile.crop((inset, inset, size - inset, size - inset))
    m = Image.new("L", tile.size, 0)
    d = ImageDraw.Draw(m)
    if squircle:
        d.rounded_rectangle((0, 0, tile.width - 1, tile.height - 1), radius=tile.width * 0.3, fill=255)
    else:
        d.ellipse((0, 0, tile.width - 1, tile.height - 1), fill=255)
    bg = Image.new("RGBA", tile.size, (240, 240, 240, 255))
    bg.paste(tile, (0, 0), m)
    return bg


def main() -> None:
    args = sys.argv[1:]
    if not args:
        sys.exit(__doc__)
    brand = Path(args[0]).expanduser().resolve()
    preview = Path(args[args.index("--preview") + 1]).resolve() if "--preview" in args else None
    props = read_props(brand / "brand.properties")
    for key in ("label", "color"):
        if key not in props:
            sys.exit(f"brand-res: brand.properties has no `{key}`")
    ground = hex_rgb(props["color"])
    icon_path = brand / "icon.png"
    if not icon_path.exists():
        sys.exit(f"brand-res: {icon_path} is missing")

    art = extract_artwork(Image.open(icon_path), ground)
    art_r = max_radius(art)
    res = brand / "res"
    largest_fg = None
    largest_splash = None
    for dpi, factor in DENSITIES.items():
        d = res / f"drawable-{dpi}"
        d.mkdir(parents=True, exist_ok=True)
        fg = place(art, art_r, round(LAUNCHER_DP * factor), LAUNCHER_SAFE_R_DP * factor)
        fg.save(d / "ic_launcher_foreground.png", optimize=True)
        sp = place(art, art_r, round(SPLASH_DP * factor), SPLASH_SAFE_R_DP * factor)
        sp.save(d / "ic_splash_quill.png", optimize=True)
        largest_fg, largest_splash = fg, sp

    label, label_en = props["label"], props.get("label.en", props["label"])
    (res / "values").mkdir(parents=True, exist_ok=True)
    (res / "values-ar").mkdir(parents=True, exist_ok=True)
    (res / "values" / "colors.xml").write_text(
        '<?xml version="1.0" encoding="utf-8"?>\n<resources>\n'
        f'    <color name="balqalam_ground">{props["color"]}</color>\n</resources>\n',
        encoding="utf-8",
    )
    for folder, value in (("values", label_en), ("values-ar", label)):
        (res / folder / "strings.xml").write_text(
            '<?xml version="1.0" encoding="utf-8"?>\n<resources>\n'
            f'    <string name="app_name">{escape(value)}</string>\n</resources>\n',
            encoding="utf-8",
        )

    if preview and largest_fg and largest_splash:
        preview.mkdir(parents=True, exist_ok=True)
        mask_preview(largest_fg, ground, squircle=False).save(preview / "launcher-circle.png")
        mask_preview(largest_fg, ground, squircle=True).save(preview / "launcher-squircle.png")
        sp = Image.new("RGBA", largest_splash.size, ground + (255,))
        sp.alpha_composite(largest_splash)
        ImageDraw.Draw(sp).ellipse(
            (sp.width / 2 - sp.width / 3, sp.height / 2 - sp.height / 3,
             sp.width / 2 + sp.width / 3, sp.height / 2 + sp.height / 3),
            outline=(255, 255, 255, 90), width=3,
        )
        sp.save(preview / "splash.png")
    print(f"brand-res: wrote {res} (label «{label}», ground {props['color']})")


if __name__ == "__main__":
    main()
