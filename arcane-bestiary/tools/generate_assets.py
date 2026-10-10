#!/usr/bin/env python3
"""Generates every texture, model, recipe, loot table, tag and language file for Arcane Bestiary.

Run from the arcane-bestiary directory:  python3 tools/generate_assets.py
Requires Pillow (pip install pillow). All output is deterministic.
"""
import json
import math
import os
import zlib

from PIL import Image

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources")
ASSETS = os.path.join(ROOT, "assets", "arcanebestiary")
DATA = os.path.join(ROOT, "data", "arcanebestiary")
MC_DATA = os.path.join(ROOT, "data", "minecraft")
NS = "arcanebestiary"


def write_json(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")


def noise(*key):
    """Deterministic pseudo random float in [0, 1) from any hashable key."""
    return (zlib.crc32(repr(key).encode()) & 0xFFFFFF) / float(0x1000000)


def shade(color, factor):
    return tuple(max(0, min(255, int(c * factor))) for c in color[:3]) + (color[3] if len(color) > 3 else 255,)


def mix(a, b, t):
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3)) + (255,)


# --------------------------------------------------------------------------------------
# Entity textures
# --------------------------------------------------------------------------------------

def box_faces(u, v, w, h, d):
    return {
        "top": (u + d, v, w, d),
        "bottom": (u + d + w, v, w, d),
        "right": (u, v + d, d, h),
        "front": (u + d, v + d, w, h),
        "left": (u + d + w, v + d, d, h),
        "back": (u + d + w + d, v + d, w, h),
    }


FACE_LIGHT = {"top": 1.15, "bottom": 0.7, "right": 0.9, "front": 1.0, "left": 0.9, "back": 0.85}

BIPED_PARTS = {
    "head": (0, 0, 8, 8, 8),
    "hat": (32, 0, 8, 8, 8),
    "body": (16, 16, 8, 12, 4),
    "rarm": (40, 16, 4, 12, 4),
    "rleg": (0, 16, 4, 12, 4),
    "larm": (32, 48, 4, 12, 4),
    "lleg": (16, 48, 4, 12, 4),
}

SPIDER_PARTS = {
    "head": (32, 4, 8, 8, 8),
    "neck": (0, 0, 6, 6, 6),
    "body": (0, 12, 10, 8, 12),
    "leg": (18, 0, 16, 2, 2),
}


def paint_model(size, parts, pixel_fn):
    """pixel_fn(part, face, lx, ly, fw, fh) -> (rgba or None, glow_rgba or None)"""
    main = Image.new("RGBA", size, (0, 0, 0, 0))
    glow = Image.new("RGBA", size, (0, 0, 0, 0))
    for part, (u, v, w, h, d) in parts.items():
        for face, (x0, y0, fw, fh) in box_faces(u, v, w, h, d).items():
            for ly in range(fh):
                for lx in range(fw):
                    if x0 + lx >= size[0] or y0 + ly >= size[1]:
                        continue
                    color, glow_color = pixel_fn(part, face, lx, ly, fw, fh)
                    if color is not None:
                        main.putpixel((x0 + lx, y0 + ly), color)
                    if glow_color is not None:
                        main.putpixel((x0 + lx, y0 + ly), glow_color)
                        glow.putpixel((x0 + lx, y0 + ly), glow_color)
    return main, glow


def textured(base, part, face, lx, ly, amp=0.18, seed=""):
    n = noise(seed, part, face, lx, ly)
    return shade(base, FACE_LIGHT[face] * (1.0 - amp / 2 + amp * n))


def is_eye(part, face, lx, ly):
    return part == "head" and face == "front" and ly in (3, 4) and lx in (1, 2, 5, 6)


def vein(part, face, lx, ly, seed, density=0.55):
    if part in ("head", "hat"):
        return False
    key = (lx * 3 + ly * 2 + int(noise(seed, part, face) * 11)) % 9
    return key == 0 and noise(seed, "v", part, face, lx, ly) < density + 0.3


def biped_style(name):
    if name == "frost_wraith":
        robe = (86, 138, 196)
        skin = (186, 226, 240)
        eye = (150, 255, 255, 255)

        def fn(part, face, lx, ly, fw, fh):
            if is_eye(part, face, lx, ly):
                return None, eye
            if part == "hat":
                return None, None
            if part == "head":
                return textured(skin, part, face, lx, ly, 0.2, name), None
            if part in ("rleg", "lleg"):
                if ly >= fh - 3 and noise(name, "rag", part, face, lx, ly) < 0.25 * (ly - fh + 4):
                    return (0, 0, 0, 0), None
                return textured(robe, part, face, lx, ly, 0.25, name), None
            if part == "body" and face in ("front", "back") and ly == 8:
                return shade((200, 230, 250), 0.9), None
            if part in ("rarm", "larm") and ly >= fh - 3:
                return textured(skin, part, face, lx, ly, 0.2, name), None
            return textured(robe, part, face, lx, ly, 0.22, name), None
        return fn

    if name == "ember_imp":
        skin = (196, 52, 28)
        horn = (40, 22, 18)
        eye = (255, 230, 60, 255)

        def fn(part, face, lx, ly, fw, fh):
            if is_eye(part, face, lx, ly):
                return None, eye
            if part == "hat":
                if face == "top" and (lx in (1, 6)) and ly in (3, 4, 5):
                    return textured(horn, part, face, lx, ly, 0.2, name), None
                if face in ("front", "back", "left", "right") and ly == 0 and lx in (1, 2, 5, 6) and face in ("front", "back"):
                    return textured(horn, part, face, lx, ly, 0.2, name), None
                return None, None
            if part == "head" and face == "front" and ly == 6 and 2 <= lx <= 5:
                return shade((60, 10, 10), 1.0), None
            if part == "body" and face == "front" and 3 <= ly <= 8 and lx in (3, 4):
                return None, (255, 140, 20, 255)
            if vein(part, face, lx, ly, name, 0.2):
                return None, (255, 150, 30, 255)
            return textured(skin, part, face, lx, ly, 0.3, name), None
        return fn

    if name.startswith("infernal_warden"):
        enraged = name.endswith("enraged")
        rock = (52, 38, 40) if not enraged else (74, 30, 28)
        lava = (255, 120, 20, 255) if not enraged else (255, 220, 70, 255)
        horn = (30, 20, 22)
        eye = (255, 220, 80, 255)

        def fn(part, face, lx, ly, fw, fh):
            if is_eye(part, face, lx, ly):
                return None, eye
            if part == "head" and face == "front" and ly == 6 and 2 <= lx <= 5:
                return None, lava
            if part == "hat":
                if face == "top" and lx in (0, 1, 6, 7):
                    return textured(horn, part, face, lx, ly, 0.2, "horn"), None
                if face in ("front", "back", "left", "right") and ly < 2 + (lx % 2) and (lx in (0, 1, 6, 7)):
                    return textured(horn, part, face, lx, ly, 0.2, "horn"), None
                return None, None
            if vein(part, face, lx, ly, name, 0.5):
                return None, lava
            if part == "body" and face in ("front", "back") and ly in (9, 10) and 1 <= lx <= 6:
                return textured((120, 70, 40), part, face, lx, ly, 0.2, name), None
            return textured(rock, part, face, lx, ly, 0.35, name), None
        return fn

    if name.startswith("glacial_sovereign"):
        enraged = name.endswith("enraged")
        robe = (46, 82, 140) if not enraged else (86, 40, 120)
        trim = (190, 230, 250)
        skull = (214, 236, 245)
        crystal = (120, 230, 255) if not enraged else (255, 130, 230)
        eye = (255, 255, 255, 255)

        def fn(part, face, lx, ly, fw, fh):
            if is_eye(part, face, lx, ly):
                return None, eye
            if part == "hat":
                if face == "top" and (lx + ly) % 2 == 0 and 1 <= lx <= 6 and 1 <= ly <= 6:
                    return None, (*crystal, 255)
                if face in ("front", "back", "left", "right"):
                    spikes = {0: (0, 2, 4, 6), 1: (0, 4)}
                    if ly in spikes and lx in spikes[ly]:
                        return None, (*crystal, 255)
                return None, None
            if part == "head":
                if face == "front" and ly == 6 and 2 <= lx <= 5 and lx % 2 == 0:
                    return shade(skull, 0.5), None
                return textured(skull, part, face, lx, ly, 0.15, name), None
            if part in ("rarm", "larm") and ly >= fh - 2:
                return textured(skull, part, face, lx, ly, 0.15, name), None
            if part in ("rleg", "lleg") and ly >= fh - 3:
                return textured(trim, part, face, lx, ly, 0.2, name), None
            if part == "body" and face in ("front", "back") and 2 <= lx <= 5 and ly in (4, 5, 6):
                return None, (*crystal, 255)
            if ly in (0, 1) and part in ("body", "rarm", "larm"):
                return textured(trim, part, face, lx, ly, 0.2, name), None
            return textured(robe, part, face, lx, ly, 0.25, name), None
        return fn
    raise ValueError(name)


def spider_style(name):
    enraged = name.endswith("enraged")
    if name.startswith("crystal_behemoth"):
        base = (44, 26, 78) if not enraged else (72, 22, 66)
        crystal = (160, 90, 255) if not enraged else (255, 90, 190)
        eye = (255, 90, 255, 255) if not enraged else (255, 230, 120, 255)
        density = 0.28
    else:
        base = (74, 50, 112)
        crystal = (200, 120, 255)
        eye = (255, 130, 255, 255)
        density = 0.18

    def fn(part, face, lx, ly, fw, fh):
        if part == "head" and face == "front" and ly in (3, 4) and lx in (1, 2, 5, 6):
            return None, eye
        if part == "head" and face == "front" and ly == 5 and lx in (3, 4):
            return None, eye
        n = noise(name, "c", part, face, lx // 2, ly // 2)
        if n < density:
            glowing = noise(name, "g", part, face, lx, ly) < 0.5
            c = (*mix(crystal, (255, 255, 255), noise(name, "w", part, face, lx, ly) * 0.4)[:3], 255)
            if glowing and part == "body":
                return None, c
            return shade(c, FACE_LIGHT[face] * 0.9), None
        return textured(base, part, face, lx, ly, 0.3, name), None
    return fn


ARMOR_LIGHT = (140, 80, 215)


def armor_pixel(part, face, lx, ly, fw, fh):
    edge = lx in (0, fw - 1) or ly in (0, fh - 1)
    n = noise("armor", part, face, lx, ly)
    facet = ((lx + ly) // 3) % 2
    base = mix((92, 48, 160), (176, 110, 245), 0.35 * facet + 0.35 * n)
    if edge:
        base = shade(base, 0.7)
    if part == "head" and face == "front" and ly in (3, 4):
        return shade(base, 0.45), None
    return shade(base, FACE_LIGHT[face]), None


def make_entity_textures():
    out = os.path.join(ASSETS, "textures", "entity")
    os.makedirs(out, exist_ok=True)
    for name in ("frost_wraith", "ember_imp"):
        main, glow = paint_model((64, 64), BIPED_PARTS, biped_style(name))
        main.save(os.path.join(out, f"{name}.png"))
        glow.save(os.path.join(out, f"{name}_glow.png"))
    for base in ("infernal_warden", "glacial_sovereign"):
        for suffix in ("", "_enraged"):
            main, glow = paint_model((64, 64), BIPED_PARTS, biped_style(base + suffix))
            main.save(os.path.join(out, f"{base}{suffix}.png"))
            glow.save(os.path.join(out, f"{base}{suffix}_glow.png"))
    main, glow = paint_model((64, 32), SPIDER_PARTS, spider_style("crystal_crawler"))
    main.save(os.path.join(out, "crystal_crawler.png"))
    glow.save(os.path.join(out, "crystal_crawler_glow.png"))
    for suffix in ("", "_enraged"):
        main, glow = paint_model((64, 32), SPIDER_PARTS, spider_style("crystal_behemoth" + suffix))
        main.save(os.path.join(out, f"crystal_behemoth{suffix}.png"))
        glow.save(os.path.join(out, f"crystal_behemoth{suffix}_glow.png"))


def make_armor_textures():
    out = os.path.join(ASSETS, "textures", "models", "armor")
    os.makedirs(out, exist_ok=True)
    layer1 = {"head": (0, 0, 8, 8, 8), "body": (16, 16, 8, 12, 4), "rarm": (40, 16, 4, 12, 4), "rleg": (0, 16, 4, 12, 4)}
    img, _ = paint_model((64, 32), layer1, armor_pixel)
    img.save(os.path.join(out, "crystalite_layer_1.png"))
    layer2 = {"body": (16, 16, 8, 12, 4), "rleg": (0, 16, 4, 12, 4)}
    img, _ = paint_model((64, 32), layer2, armor_pixel)
    img.save(os.path.join(out, "crystalite_layer_2.png"))


# --------------------------------------------------------------------------------------
# 16x16 item art
# --------------------------------------------------------------------------------------

class Canvas:
    def __init__(self):
        self.px = {}

    def set(self, x, y, c):
        if 0 <= x < 16 and 0 <= y < 16:
            self.px[(x, y)] = c

    def line(self, x0, y0, x1, y1, c):
        n = max(abs(x1 - x0), abs(y1 - y0), 1)
        for i in range(n + 1):
            self.set(round(x0 + (x1 - x0) * i / n), round(y0 + (y1 - y0) * i / n), c)

    def polyline(self, pts, c):
        for a, b in zip(pts, pts[1:]):
            self.line(a[0], a[1], b[0], b[1], c)

    def poly(self, pts, colorfn):
        for y in range(16):
            for x in range(16):
                if inside(pts, x + 0.5, y + 0.5):
                    self.set(x, y, colorfn(x, y))

    def rect(self, x, y, w, h, colorfn):
        for yy in range(y, y + h):
            for xx in range(x, x + w):
                self.set(xx, yy, colorfn(xx, yy) if callable(colorfn) else colorfn)

    def ellipse(self, cx, cy, rx, ry, colorfn):
        for y in range(16):
            for x in range(16):
                if ((x + 0.5 - cx) / rx) ** 2 + ((y + 0.5 - cy) / ry) ** 2 <= 1.0:
                    self.set(x, y, colorfn(x, y))

    def outline(self, color=(20, 14, 24, 255)):
        filled = set(self.px)
        for (x, y) in filled:
            for dx in (-1, 0, 1):
                for dy in (-1, 0, 1):
                    n = (x + dx, y + dy)
                    if n not in filled and 0 <= n[0] < 16 and 0 <= n[1] < 16 and (dx == 0 or dy == 0):
                        self.px[n] = color

    def save(self, path):
        img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
        for (x, y), c in self.px.items():
            img.putpixel((x, y), c)
        os.makedirs(os.path.dirname(path), exist_ok=True)
        img.save(path)
        return img


def inside(pts, x, y):
    result = False
    j = len(pts) - 1
    for i in range(len(pts)):
        xi, yi = pts[i]
        xj, yj = pts[j]
        if (yi > y) != (yj > y) and x < (xj - xi) * (y - yi) / (yj - yi + 1e-9) + xi:
            result = not result
        j = i
    return result


WOOD = (126, 88, 52, 255)
WOOD_DARK = (88, 58, 32, 255)


def gradient(c1, c2, name="g"):
    """Diagonal gradient with a little noise, light from the top-left."""
    def fn(x, y):
        t = (x + y) / 30.0
        t = min(1.0, max(0.0, t + (noise(name, x, y) - 0.5) * 0.18))
        return mix(c1, c2, t)
    return fn


def handle(cv, x0, y0, x1, y1):
    cv.line(x0, y0, x1, y1, WOOD)
    cv.line(x0 + 1, y0, x1 + 1, y1, WOOD_DARK)


def sword(light, dark, guard, grip=WOOD, glow=None):
    cv = Canvas()
    for i in range(5, 15):
        cv.set(i, 15 - i, light)
        cv.set(i - 1, 15 - i, dark)
    cv.set(14, 1, light)
    cv.set(15, 0, light)
    cv.line(3, 8, 7, 12, guard)
    cv.line(2, 8, 6, 12, shade(guard, 0.8))
    cv.line(4, 11, 2, 13, grip)
    cv.set(1, 14, guard)
    if glow:
        for i in range(7, 13, 2):
            cv.set(i - 1, 15 - i, glow)
    cv.outline()
    return cv


def pickaxe(head_light, head_dark):
    cv = Canvas()
    cv.line(2, 14, 11, 5, WOOD)
    cv.line(2, 13, 10, 5, WOOD_DARK)
    cv.polyline([(3, 7), (4, 4), (7, 2), (11, 2), (13, 4), (14, 7)], head_light)
    cv.polyline([(4, 8), (5, 5), (7, 3), (11, 3), (12, 5), (13, 7)], head_dark)
    cv.set(10, 4, head_dark)
    cv.set(11, 4, head_dark)
    cv.outline()
    return cv


def axe(head_light, head_dark):
    cv = Canvas()
    cv.line(2, 14, 11, 5, WOOD)
    cv.line(2, 13, 10, 5, WOOD_DARK)
    cv.poly([(7, 3), (11, 1), (14, 3), (14, 7), (11, 7), (9, 6)], gradient(head_light, head_dark, "axe"))
    cv.outline()
    return cv


def shovel(head_light, head_dark):
    cv = Canvas()
    cv.line(2, 14, 11, 5, WOOD)
    cv.line(2, 13, 10, 5, WOOD_DARK)
    cv.ellipse(12.5, 3.5, 3.0, 3.0, gradient(head_light, head_dark, "shovel"))
    cv.outline()
    return cv


def hoe(head_light, head_dark):
    cv = Canvas()
    cv.line(2, 14, 11, 5, WOOD)
    cv.line(2, 13, 10, 5, WOOD_DARK)
    cv.polyline([(6, 4), (8, 2), (12, 1), (14, 2)], head_light)
    cv.polyline([(6, 5), (8, 3), (12, 2), (14, 3)], head_dark)
    cv.line(6, 4, 6, 7, head_dark)
    cv.outline()
    return cv


def staff():
    cv = Canvas()
    cv.line(2, 14, 10, 6, WOOD)
    cv.line(2, 13, 9, 6, WOOD_DARK)
    cv.line(7, 9, 10, 12, (200, 200, 60, 255))
    cv.ellipse(11.5, 4.5, 3.4, 3.4, lambda x, y: mix((255, 240, 120), (80, 200, 255), min(1, (x + y) / 26)))
    cv.set(11, 3, (255, 255, 255, 255))
    cv.set(12, 3, (255, 255, 255, 255))
    for p in [(8, 1), (14, 7), (14, 1)]:
        cv.set(*p, (255, 255, 160, 255))
    cv.outline()
    return cv


def maul():
    cv = Canvas()
    cv.line(2, 14, 9, 7, WOOD)
    cv.line(2, 13, 8, 7, WOOD_DARK)
    cv.poly([(5, 6), (10, 1), (15, 6), (10, 11)], gradient((200, 130, 255), (70, 36, 130), "maul"))
    cv.line(7, 4, 12, 9, (255, 220, 255, 255))
    cv.line(10, 2, 14, 6, (230, 170, 255, 255))
    cv.outline()
    return cv


def shard():
    cv = Canvas()
    cv.poly([(8, 0.5), (12, 6), (9.5, 15), (6.5, 15), (4, 6)], gradient((235, 180, 255), (100, 50, 180), "shard"))
    cv.line(8, 2, 8, 12, (255, 235, 255, 255))
    cv.line(6, 6, 7, 11, (160, 90, 230, 255))
    cv.outline()
    return cv


def essence(kind):
    cv = Canvas()
    if kind == "ember":
        cv.poly([(8, 0.5), (12, 6), (13, 10), (11, 14), (5, 14), (3, 10), (4, 6)], gradient((255, 230, 80), (200, 40, 10), "ember"))
        cv.poly([(8, 5), (10, 9), (9, 12), (7, 12), (6, 9)], lambda x, y: (255, 245, 170, 255))
    else:
        cv.ellipse(8, 8, 5.5, 5.5, gradient((240, 255, 255), (80, 150, 220), "frost"))
        cv.line(8, 2, 8, 13, (255, 255, 255, 255))
        cv.line(2, 8, 13, 8, (255, 255, 255, 255))
        cv.line(4, 4, 11, 11, (210, 245, 255, 255))
        cv.line(11, 4, 4, 11, (210, 245, 255, 255))
    cv.outline()
    return cv


def core(kind):
    cv = Canvas()
    if kind == "infernal":
        c1, c2, ring = (255, 220, 90), (150, 20, 10), (50, 30, 34, 255)
    else:
        c1, c2, ring = (230, 255, 255), (30, 90, 190), (170, 215, 235, 255)
    cv.ellipse(8, 8, 6.5, 6.5, lambda x, y: ring)
    cv.ellipse(8, 8, 4.6, 4.6, gradient(c1, c2, kind))
    cv.set(7, 6, (255, 255, 255, 255))
    cv.set(6, 7, (255, 255, 255, 255))
    for p in [(8, 1), (8, 14), (1, 8), (14, 8)]:
        cv.set(*p, ring)
    cv.outline()
    return cv


def heart():
    cv = Canvas()
    cv.poly([(8, 14.5), (1.5, 7.5), (1.5, 4), (4, 1.5), (6.5, 1.5), (8, 3.5), (9.5, 1.5), (12, 1.5), (14.5, 4), (14.5, 7.5)],
            gradient((255, 170, 255), (110, 40, 190), "heart"))
    cv.line(4, 4, 6, 3, (255, 240, 255, 255))
    cv.line(8, 5, 8, 11, (230, 140, 255, 255))
    cv.outline()
    return cv


def sigil(kind):
    cv = Canvas()
    colors = {
        "infernal": ((255, 190, 70), (150, 20, 10), (255, 240, 120, 255)),
        "glacial": ((220, 250, 255), (50, 120, 210), (255, 255, 255, 255)),
        "crystal": ((240, 180, 255), (90, 40, 170), (255, 235, 255, 255)),
    }
    c1, c2, rune = colors[kind]
    cv.ellipse(8, 8, 7, 7, gradient(c1, c2, kind))
    cv.ellipse(8, 8, 5.2, 5.2, lambda x, y: shade(mix(c2, (10, 6, 16), 0.4), 1.0))
    pts = [(8 + 4 * math.sin(i * 4 * math.pi / 5), 8 - 4 * math.cos(i * 4 * math.pi / 5)) for i in range(6)]
    for a, b in zip(pts, pts[1:]):
        cv.line(round(a[0]), round(a[1]), round(b[0]), round(b[1]), rune)
    cv.outline()
    return cv


def solar_cell():
    cv = Canvas()
    cv.poly([(3, 1), (14, 1), (12.5, 15), (1.5, 15)], lambda x, y: mix((16, 40, 110), (60, 120, 220), min(1, (x + (15 - y)) / 22 + (noise("cell", x, y) - 0.5) * 0.2)))
    for x in (6, 10):
        cv.line(x, 2, x - 1, 14, (200, 205, 215, 255))
    for y in (6, 10):
        cv.line(2, y, 13, y, (200, 205, 215, 255))
    cv.set(4, 3, (170, 220, 255, 255))
    cv.set(5, 3, (170, 220, 255, 255))
    cv.outline()
    return cv


def crystal_color(name):
    def fn(x, y):
        n = noise(name, x, y)
        facet = ((x + y) // 3) % 2
        return mix((96, 50, 164), (190, 120, 250), 0.4 * facet + 0.4 * n)
    return fn


def armor_piece(kind):
    cv = Canvas()
    f = crystal_color(kind)
    if kind == "helmet":
        cv.rect(3, 3, 10, 7, f)
        cv.rect(3, 10, 2, 3, f)
        cv.rect(11, 10, 2, 3, f)
        cv.rect(5, 7, 6, 3, lambda x, y: (20, 10, 34, 255))
        cv.rect(7, 1, 2, 2, lambda x, y: (240, 190, 255, 255))
    elif kind == "chestplate":
        cv.rect(4, 2, 8, 12, f)
        cv.rect(0, 2, 4, 6, f)
        cv.rect(12, 2, 4, 6, f)
        cv.rect(0, 8, 3, 3, shade((120, 70, 190), 0.9))
        cv.rect(13, 8, 3, 3, shade((120, 70, 190), 0.9))
        cv.rect(6, 2, 4, 2, lambda x, y: (20, 10, 34, 255))
        cv.line(8, 5, 8, 12, (240, 190, 255, 255))
    elif kind == "leggings":
        cv.rect(3, 2, 10, 4, f)
        cv.rect(3, 6, 4, 8, f)
        cv.rect(9, 6, 4, 8, f)
        cv.rect(3, 4, 10, 1, (240, 190, 255, 255))
    elif kind == "boots":
        cv.rect(2, 6, 4, 6, f)
        cv.rect(10, 6, 4, 6, f)
        cv.rect(1, 12, 6, 2, f)
        cv.rect(9, 12, 6, 2, f)
        cv.rect(2, 6, 4, 1, (240, 190, 255, 255))
        cv.rect(10, 6, 4, 1, (240, 190, 255, 255))
    cv.outline()
    return cv


def make_item_textures():
    out = os.path.join(ASSETS, "textures", "item")
    cr_l, cr_d = (226, 160, 255, 255), (130, 74, 200, 255)
    items = {
        "crystalite_sword": sword(cr_l, cr_d, (90, 60, 150, 255), glow=(255, 235, 255, 255)),
        "crystalite_pickaxe": pickaxe(cr_l, cr_d),
        "crystalite_axe": axe(cr_l, cr_d),
        "crystalite_shovel": shovel(cr_l, cr_d),
        "crystalite_hoe": hoe(cr_l, cr_d),
        "emberbrand": sword((255, 214, 90, 255), (214, 70, 14, 255), (60, 36, 36, 255), glow=(255, 250, 190, 255)),
        "frostfang": sword((240, 255, 255, 255), (96, 170, 235, 255), (70, 110, 170, 255), glow=(255, 255, 255, 255)),
        "tempest_staff": staff(),
        "behemoth_maul": maul(),
        "crystalite_shard": shard(),
        "solar_cell": solar_cell(),
        "ember_essence": essence("ember"),
        "frost_essence": essence("frost"),
        "infernal_core": core("infernal"),
        "glacial_core": core("glacial"),
        "crystal_heart": heart(),
        "infernal_sigil": sigil("infernal"),
        "glacial_sigil": sigil("glacial"),
        "crystal_sigil": sigil("crystal"),
        "crystalite_helmet": armor_piece("helmet"),
        "crystalite_chestplate": armor_piece("chestplate"),
        "crystalite_leggings": armor_piece("leggings"),
        "crystalite_boots": armor_piece("boots"),
    }
    imgs = {}
    for name, cv in items.items():
        imgs[name] = cv.save(os.path.join(out, name + ".png"))
    return imgs


def make_block_textures():
    out = os.path.join(ASSETS, "textures", "block")
    os.makedirs(out, exist_ok=True)
    ore = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            n = noise("stone", x, y)
            ore.putpixel((x, y), shade((124, 124, 130, 255), 0.82 + 0.3 * n))
    for cx, cy in [(3, 3), (11, 4), (6, 10), (12, 12), (2, 13)]:
        for dx, dy in [(0, 0), (1, 0), (0, 1), (1, 1), (0, -1), (-1, 0)]:
            x, y = cx + dx, cy + dy
            if 0 <= x < 16 and 0 <= y < 16:
                ore.putpixel((x, y), mix((120, 60, 200), (240, 180, 255), noise("ore", x, y)))
        ore.putpixel((cx, cy), (255, 240, 255, 255))
    ore.save(os.path.join(out, "crystalite_ore.png"))

    block = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            f = ((x // 4) + (y // 4)) % 2
            t = 0.3 * f + 0.35 * noise("block", x, y) + 0.2 * (1 - (x + y) / 30)
            c = mix((84, 40, 150), (205, 140, 255), t)
            if x % 4 == 0 or y % 4 == 0:
                c = shade(c, 0.8)
            block.putpixel((x, y), c)
    block.save(os.path.join(out, "crystalite_block.png"))


def make_solar_textures():
    out = os.path.join(ASSETS, "textures", "block")
    frame = (176, 182, 192, 255)
    for active in (False, True):
        top = Image.new("RGBA", (16, 16))
        c1, c2 = ((10, 24, 70), (30, 70, 150)) if not active else ((30, 90, 200), (120, 200, 255))
        for y in range(16):
            for x in range(16):
                if x in (0, 15) or y in (0, 15):
                    top.putpixel((x, y), shade(frame, 0.85 + 0.2 * noise("frame", x, y)))
                elif x in (7, 8) or y in (7, 8):
                    top.putpixel((x, y), shade(frame, 0.9))
                else:
                    t = ((x + (15 - y)) / 30.0) * 0.6 + noise("solar", x, y) * 0.25
                    top.putpixel((x, y), mix(c1, c2, min(1.0, t)))
        for (x, y) in [(2, 2), (3, 2), (2, 3), (10, 9), (11, 9)]:
            top.putpixel((x, y), (200, 235, 255, 255) if not active else (255, 255, 255, 255))
        top.save(os.path.join(out, "solar_panel_top_active.png" if active else "solar_panel_top.png"))
    side = Image.new("RGBA", (16, 16))
    bottom = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            n = noise("metal", x, y)
            side.putpixel((x, y), shade(frame, 0.7 + 0.25 * n) if y < 4 else (0, 0, 0, 0))
            bottom.putpixel((x, y), shade((110, 114, 124, 255), 0.85 + 0.25 * n))
    for x in range(16):
        side.putpixel((x, 0), shade(frame, 1.1))
    side.save(os.path.join(out, "solar_panel_side.png"))
    bottom.save(os.path.join(out, "solar_panel_bottom.png"))


def make_icon(imgs):
    icon = imgs["emberbrand"].resize((128, 128), Image.NEAREST)
    bg = Image.new("RGBA", (128, 128), (30, 20, 48, 255))
    bg.alpha_composite(icon)
    bg.save(os.path.join(ASSETS, "icon.png"))


# --------------------------------------------------------------------------------------
# Models, recipes, loot, tags, worldgen, lang
# --------------------------------------------------------------------------------------

HANDHELD = ["crystalite_sword", "crystalite_pickaxe", "crystalite_axe", "crystalite_shovel", "crystalite_hoe",
            "emberbrand", "frostfang", "tempest_staff", "behemoth_maul"]
GENERATED = ["crystalite_shard", "solar_cell", "ember_essence", "frost_essence", "infernal_core", "glacial_core", "crystal_heart",
             "infernal_sigil", "glacial_sigil", "crystal_sigil",
             "crystalite_helmet", "crystalite_chestplate", "crystalite_leggings", "crystalite_boots"]
EGGS = ["frost_wraith", "ember_imp", "crystal_crawler", "infernal_warden", "glacial_sovereign", "crystal_behemoth"]
BLOCKS = ["crystalite_ore", "crystalite_block"]


def make_models():
    for name in HANDHELD:
        write_json(os.path.join(ASSETS, "models", "item", name + ".json"),
                   {"parent": "minecraft:item/handheld", "textures": {"layer0": f"{NS}:item/{name}"}})
    for name in GENERATED:
        write_json(os.path.join(ASSETS, "models", "item", name + ".json"),
                   {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/{name}"}})
    for name in EGGS:
        write_json(os.path.join(ASSETS, "models", "item", name + "_spawn_egg.json"),
                   {"parent": "minecraft:item/template_spawn_egg"})
    for name in BLOCKS:
        write_json(os.path.join(ASSETS, "blockstates", name + ".json"),
                   {"variants": {"": {"model": f"{NS}:block/{name}"}}})
        write_json(os.path.join(ASSETS, "models", "block", name + ".json"),
                   {"parent": "minecraft:block/cube_all", "textures": {"all": f"{NS}:block/{name}"}})
        write_json(os.path.join(ASSETS, "models", "item", name + ".json"), {"parent": f"{NS}:block/{name}"})


def make_solar_models():
    for active in (False, True):
        name = "solar_panel_active" if active else "solar_panel"
        write_json(os.path.join(ASSETS, "models", "block", name + ".json"), {
            "parent": "minecraft:block/block",
            "textures": {
                "top": f"{NS}:block/solar_panel_top" + ("_active" if active else ""),
                "side": f"{NS}:block/solar_panel_side",
                "bottom": f"{NS}:block/solar_panel_bottom",
                "particle": f"{NS}:block/solar_panel_bottom",
            },
            "elements": [{
                "from": [0, 0, 0], "to": [16, 4, 16],
                "faces": {
                    "up": {"uv": [0, 0, 16, 16], "texture": "#top"},
                    "down": {"uv": [0, 0, 16, 16], "texture": "#bottom", "cullface": "down"},
                    "north": {"uv": [0, 0, 16, 4], "texture": "#side", "cullface": "north"},
                    "south": {"uv": [0, 0, 16, 4], "texture": "#side", "cullface": "south"},
                    "west": {"uv": [0, 0, 16, 4], "texture": "#side", "cullface": "west"},
                    "east": {"uv": [0, 0, 16, 4], "texture": "#side", "cullface": "east"},
                },
            }],
        })
    write_json(os.path.join(ASSETS, "blockstates", "solar_panel.json"), {"variants": {
        f"power={p}": {"model": f"{NS}:block/solar_panel" + ("_active" if p > 0 else "")} for p in range(16)}})
    write_json(os.path.join(ASSETS, "models", "item", "solar_panel.json"), {"parent": f"{NS}:block/solar_panel_active"})


def shaped(name, pattern, key, result, count=1):
    write_json(os.path.join(DATA, "recipe", name + ".json"), {
        "type": "minecraft:crafting_shaped",
        "category": "equipment",
        "pattern": pattern,
        "key": {k: {"item": v} for k, v in key.items()},
        "result": {"id": result, "count": count},
    })


def shapeless(name, ingredients, result, count=1, category="misc"):
    write_json(os.path.join(DATA, "recipe", name + ".json"), {
        "type": "minecraft:crafting_shapeless",
        "category": category,
        "ingredients": [{"item": i} for i in ingredients],
        "result": {"id": result, "count": count},
    })


def make_recipes():
    s = f"{NS}:crystalite_shard"
    stick = "minecraft:stick"
    shaped("crystalite_sword", [" S ", " S ", " T "], {"S": s, "T": stick}, f"{NS}:crystalite_sword")
    shaped("crystalite_pickaxe", ["SSS", " T ", " T "], {"S": s, "T": stick}, f"{NS}:crystalite_pickaxe")
    shaped("crystalite_axe", ["SS ", "ST ", " T "], {"S": s, "T": stick}, f"{NS}:crystalite_axe")
    shaped("crystalite_shovel", [" S ", " T ", " T "], {"S": s, "T": stick}, f"{NS}:crystalite_shovel")
    shaped("crystalite_hoe", ["SS ", " T ", " T "], {"S": s, "T": stick}, f"{NS}:crystalite_hoe")
    shaped("crystalite_helmet", ["SSS", "S S"], {"S": s}, f"{NS}:crystalite_helmet")
    shaped("crystalite_chestplate", ["S S", "SSS", "SSS"], {"S": s}, f"{NS}:crystalite_chestplate")
    shaped("crystalite_leggings", ["SSS", "S S", "S S"], {"S": s}, f"{NS}:crystalite_leggings")
    shaped("crystalite_boots", ["S S", "S S"], {"S": s}, f"{NS}:crystalite_boots")
    shaped("crystalite_block", ["SSS", "SSS", "SSS"], {"S": s}, f"{NS}:crystalite_block")
    shapeless("crystalite_shard_from_block", [f"{NS}:crystalite_block"], s, 9)

    shaped("solar_cell", ["GGG", "QSQ"],
           {"G": "minecraft:glass_pane", "Q": "minecraft:quartz", "S": s}, f"{NS}:solar_cell", 2)
    shaped("solar_panel", ["CCC", "IRI", "SSS"],
           {"C": f"{NS}:solar_cell", "I": "minecraft:iron_ingot", "R": "minecraft:redstone", "S": "minecraft:smooth_stone_slab"},
           f"{NS}:solar_panel")

    shaped("infernal_sigil", ["EBE", "BSB", "EBE"],
           {"E": f"{NS}:ember_essence", "B": "minecraft:blaze_rod", "S": s}, f"{NS}:infernal_sigil")
    shaped("glacial_sigil", ["FIF", "ISI", "FIF"],
           {"F": f"{NS}:frost_essence", "I": "minecraft:blue_ice", "S": s}, f"{NS}:glacial_sigil")
    shaped("crystal_sigil", ["SAS", "ADA", "SAS"],
           {"S": s, "A": "minecraft:amethyst_shard", "D": "minecraft:diamond"}, f"{NS}:crystal_sigil")
    shaped("tempest_staff", ["ICG", " R ", " R "],
           {"I": f"{NS}:infernal_core", "C": s, "G": f"{NS}:glacial_core", "R": "minecraft:blaze_rod"}, f"{NS}:tempest_staff")


def item_entry(item, lo=None, hi=None, looting=None):
    functions = []
    if lo is not None:
        functions.append({"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": lo, "max": hi}})
    if looting:
        functions.append({"function": "minecraft:enchanted_count_increase", "enchantment": "minecraft:looting",
                          "count": {"type": "minecraft:uniform", "min": 0, "max": looting}})
    entry = {"type": "minecraft:item", "name": item}
    if functions:
        entry["functions"] = functions
    return entry


def entity_loot(name, pools):
    write_json(os.path.join(DATA, "loot_table", "entities", name + ".json"), {
        "type": "minecraft:entity",
        "pools": [{"rolls": 1, "entries": [e]} for e in pools],
    })


def make_loot():
    entity_loot("frost_wraith", [item_entry(f"{NS}:frost_essence", 0, 2, 1), item_entry("minecraft:snowball", 0, 4)])
    entity_loot("ember_imp", [item_entry(f"{NS}:ember_essence", 0, 2, 1), item_entry("minecraft:blaze_powder", 0, 1)])
    entity_loot("crystal_crawler", [item_entry(f"{NS}:crystalite_shard", 0, 2, 1), item_entry("minecraft:string", 0, 2)])
    entity_loot("infernal_warden", [
        item_entry(f"{NS}:emberbrand"), item_entry(f"{NS}:infernal_core"),
        item_entry(f"{NS}:ember_essence", 8, 16), item_entry("minecraft:diamond", 2, 5),
        item_entry("minecraft:netherite_scrap", 1, 2), item_entry(f"{NS}:crystalite_shard", 4, 8)])
    entity_loot("glacial_sovereign", [
        item_entry(f"{NS}:frostfang"), item_entry(f"{NS}:glacial_core"),
        item_entry(f"{NS}:frost_essence", 8, 16), item_entry("minecraft:diamond", 2, 5),
        item_entry("minecraft:blue_ice", 8, 16), item_entry(f"{NS}:crystalite_shard", 4, 8)])
    entity_loot("crystal_behemoth", [
        item_entry(f"{NS}:behemoth_maul"), item_entry(f"{NS}:crystal_heart"),
        item_entry(f"{NS}:crystalite_shard", 12, 24), item_entry("minecraft:diamond", 3, 6),
        item_entry("minecraft:amethyst_shard", 16, 32)])

    write_json(os.path.join(DATA, "loot_table", "blocks", "crystalite_block.json"), {
        "type": "minecraft:block",
        "pools": [{"rolls": 1, "conditions": [{"condition": "minecraft:survives_explosion"}],
                   "entries": [{"type": "minecraft:item", "name": f"{NS}:crystalite_block"}]}],
    })
    write_json(os.path.join(DATA, "loot_table", "blocks", "solar_panel.json"), {
        "type": "minecraft:block",
        "pools": [{"rolls": 1, "conditions": [{"condition": "minecraft:survives_explosion"}],
                   "entries": [{"type": "minecraft:item", "name": f"{NS}:solar_panel"}]}],
    })
    silk = {"condition": "minecraft:match_tool", "predicate": {"predicates": {"minecraft:enchantments": [
        {"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}}}
    write_json(os.path.join(DATA, "loot_table", "blocks", "crystalite_ore.json"), {
        "type": "minecraft:block",
        "pools": [{"rolls": 1, "entries": [{
            "type": "minecraft:alternatives",
            "children": [
                {"type": "minecraft:item", "name": f"{NS}:crystalite_ore", "conditions": [silk]},
                {"type": "minecraft:item", "name": f"{NS}:crystalite_shard", "functions": [
                    {"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 3}},
                    {"function": "minecraft:apply_bonus", "enchantment": "minecraft:fortune", "formula": "minecraft:ore_drops"},
                    {"function": "minecraft:explosion_decay"}]},
            ]}]}],
    })


def make_tags_and_worldgen():
    both = [f"{NS}:crystalite_ore", f"{NS}:crystalite_block"]
    write_json(os.path.join(MC_DATA, "tags", "block", "mineable", "pickaxe.json"), {"replace": False, "values": both + [f"{NS}:solar_panel"]})
    write_json(os.path.join(MC_DATA, "tags", "block", "needs_iron_tool.json"), {"replace": False, "values": both})
    state = {"Name": f"{NS}:crystalite_ore"}
    write_json(os.path.join(DATA, "worldgen", "configured_feature", "crystalite_ore.json"), {
        "type": "minecraft:ore",
        "config": {
            "discard_chance_on_air_exposure": 0.0,
            "size": 7,
            "targets": [
                {"state": state, "target": {"predicate_type": "minecraft:tag_match", "tag": "minecraft:stone_ore_replaceables"}},
                {"state": state, "target": {"predicate_type": "minecraft:tag_match", "tag": "minecraft:deepslate_ore_replaceables"}},
            ],
        },
    })
    write_json(os.path.join(DATA, "worldgen", "placed_feature", "crystalite_ore.json"), {
        "feature": f"{NS}:crystalite_ore",
        "placement": [
            {"type": "minecraft:count", "count": 5},
            {"type": "minecraft:in_square"},
            {"type": "minecraft:height_range", "height": {
                "type": "minecraft:trapezoid",
                "min_inclusive": {"absolute": -60}, "max_inclusive": {"absolute": 30}}},
            {"type": "minecraft:biome"},
        ],
    })


def make_lang():
    lang = {
        "itemGroup.arcanebestiary.main": "Arcane Bestiary",
        "block.arcanebestiary.crystalite_ore": "Crystalite Ore",
        "block.arcanebestiary.crystalite_block": "Block of Crystalite",
        "item.arcanebestiary.crystalite_shard": "Crystalite Shard",
        "block.arcanebestiary.solar_panel": "Solar Panel",
        "block.arcanebestiary.solar_panel.tooltip.repair": "Use with a damaged item to repair it in sunlight",
        "block.arcanebestiary.solar_panel.tooltip.redstone": "Emits a redstone signal equal to the sunlight level",
        "item.arcanebestiary.solar_cell": "Solar Cell",
        "item.arcanebestiary.ember_essence": "Ember Essence",
        "item.arcanebestiary.frost_essence": "Frost Essence",
        "item.arcanebestiary.infernal_core": "Infernal Core",
        "item.arcanebestiary.glacial_core": "Glacial Core",
        "item.arcanebestiary.crystal_heart": "Crystal Heart",
        "item.arcanebestiary.crystalite_sword": "Crystalite Sword",
        "item.arcanebestiary.crystalite_pickaxe": "Crystalite Pickaxe",
        "item.arcanebestiary.crystalite_axe": "Crystalite Axe",
        "item.arcanebestiary.crystalite_shovel": "Crystalite Shovel",
        "item.arcanebestiary.crystalite_hoe": "Crystalite Hoe",
        "item.arcanebestiary.crystalite_helmet": "Crystalite Helmet",
        "item.arcanebestiary.crystalite_chestplate": "Crystalite Chestplate",
        "item.arcanebestiary.crystalite_leggings": "Crystalite Leggings",
        "item.arcanebestiary.crystalite_boots": "Crystalite Boots",
        "item.arcanebestiary.emberbrand": "Emberbrand",
        "item.arcanebestiary.emberbrand.tooltip.hit": "Sets struck enemies ablaze",
        "item.arcanebestiary.emberbrand.tooltip.use": "Use: hurl a fireball",
        "item.arcanebestiary.frostfang": "Frostfang",
        "item.arcanebestiary.frostfang.tooltip.hit": "Chills and slows struck enemies",
        "item.arcanebestiary.frostfang.tooltip.use": "Use: unleash a frost nova",
        "item.arcanebestiary.behemoth_maul": "Behemoth Maul",
        "item.arcanebestiary.behemoth_maul.tooltip.hit": "Hits send a shockwave through nearby enemies",
        "item.arcanebestiary.tempest_staff": "Tempest Staff",
        "item.arcanebestiary.tempest_staff.tooltip.use": "Use: call lightning on your target",
        "item.arcanebestiary.tempest_staff.tooltip.sneak": "Sneak + use: summon a lightning storm",
        "item.arcanebestiary.infernal_sigil": "Infernal Sigil",
        "item.arcanebestiary.infernal_sigil.tooltip": "Use on the ground to summon the Infernal Warden",
        "item.arcanebestiary.glacial_sigil": "Glacial Sigil",
        "item.arcanebestiary.glacial_sigil.tooltip": "Use on the ground to summon the Glacial Sovereign",
        "item.arcanebestiary.crystal_sigil": "Crystal Sigil",
        "item.arcanebestiary.crystal_sigil.tooltip": "Use on the ground to summon the Crystal Behemoth",
        "message.arcanebestiary.sigil.peaceful": "The sigil fizzles. The darkness will not answer in peaceful.",
    }
    for name, label in [("frost_wraith", "Frost Wraith"), ("ember_imp", "Ember Imp"), ("crystal_crawler", "Crystal Crawler"),
                        ("infernal_warden", "Infernal Warden"), ("glacial_sovereign", "Glacial Sovereign"),
                        ("crystal_behemoth", "Crystal Behemoth")]:
        lang[f"entity.arcanebestiary.{name}"] = label
        lang[f"item.arcanebestiary.{name}_spawn_egg"] = f"{label} Spawn Egg"
    lang["entity.arcanebestiary.frost_bolt"] = "Frost Bolt"
    write_json(os.path.join(ASSETS, "lang", "en_us.json"), lang)


if __name__ == "__main__":
    make_entity_textures()
    make_armor_textures()
    item_imgs = make_item_textures()
    make_block_textures()
    make_solar_textures()
    make_icon(item_imgs)
    make_models()
    make_solar_models()
    make_recipes()
    make_loot()
    make_tags_and_worldgen()
    make_lang()
    print("assets generated")
