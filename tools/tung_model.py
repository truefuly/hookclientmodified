"""Builds tung.glb: a clean, rigged 3D "Tung Tung Tung Sahur" (wooden log with face, stick limbs and a bat).

Pure Python (no numpy). Run: python tools/tung_model.py out.glb
The game bakes the GLB into its own .anc format (put it in config/hook/characters).
"""
import json
import math
import random
import struct
import sys
import zlib

random.seed(7)

# ---------------------------------------------------------------- textures
def png(w, h, pixels):
    raw = b"".join(b"\x00" + bytes(pixels[y * w * 4:(y + 1) * w * 4]) for y in range(h))
    def chunk(t, d):
        c = struct.pack(">I", len(d)) + t + d
        return c + struct.pack(">I", zlib.crc32(t + d) & 0xFFFFFFFF)
    return (b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0))
            + chunk(b"IDAT", zlib.compress(raw, 9)) + chunk(b"IEND", b""))

def noise1(n):
    pts = [random.random() for _ in range(n + 1)]
    pts[-1] = pts[0]
    def f(t):
        t = (t % 1.0) * n
        i = int(t)
        a = t - i
        a = a * a * (3 - 2 * a)
        return pts[i] * (1 - a) + pts[i + 1] * a
    return f

def clamp8(v):
    return max(0, min(255, int(v)))

def wood_texture(size=256):
    """Light sanded wood with vertical grain and two small knots."""
    warp = noise1(12)
    grain = noise1(40)
    fine = noise1(160)
    knots = [(0.3, 0.35, 0.035), (0.78, 0.7, 0.025)]
    px = []
    for y in range(size):
        v = y / size
        for x in range(size):
            u = x / size
            k = 0.0
            du = u
            for (kx, ky, kr) in knots:
                dx = min(abs(u - kx), 1 - abs(u - kx)) * 2.2
                dy = v - ky
                d = math.hypot(dx, dy)
                if d < kr * 3:
                    du += (kr * 3 - d) * 0.6 * (1 if u > kx else -1)
                if d < kr:
                    k = max(k, 1 - d / kr)
            g = grain((du + warp(v) * 0.04) % 1.0)
            line = 0.5 + 0.5 * math.sin((du * 34 + warp(v * 0.7) * 3) * math.pi * 2)
            t = 0.55 * g + 0.25 * line + 0.2 * fine(du % 1.0)
            r = 214 - 46 * t
            gg = 170 - 44 * t
            b = 118 - 36 * t
            if k > 0:
                r, gg, b = r * (1 - 0.55 * k), gg * (1 - 0.6 * k), b * (1 - 0.6 * k)
            px += [clamp8(r), clamp8(gg), clamp8(b), 255]
    return png(size, size, px)

def rings_texture(size=128):
    """End grain: concentric growth rings."""
    wob = noise1(9)
    px = []
    for y in range(size):
        for x in range(size):
            dx = (x + 0.5) / size - 0.5
            dy = (y + 0.5) / size - 0.5
            r = math.hypot(dx, dy) * 2
            a = (math.atan2(dy, dx) / (2 * math.pi)) % 1.0
            rr = r + wob(a) * 0.05
            ring = 0.5 + 0.5 * math.cos(rr * 9 * math.pi * 2)
            t = 0.65 * ring + 0.35 * r
            px += [clamp8(222 - 50 * t), clamp8(184 - 52 * t), clamp8(132 - 44 * t), 255]
    return png(size, size, px)

# ---------------------------------------------------------------- skeleton
# name, parent, global head position (glTF: +Y up, the model faces +Z, its left side is +X)
BONES = [
    ("Hips", None, (0.0, 0.76, 0.0)),
    ("Spine", "Hips", (0.0, 0.95, 0.0)),
    ("Head", "Spine", (0.0, 1.27, 0.0)),
    ("LeftUpperArm", "Spine", (0.25, 1.13, 0.0)),
    ("LeftLowerArm", "LeftUpperArm", (0.30, 0.87, 0.0)),
    ("LeftHand", "LeftLowerArm", (0.31, 0.63, 0.0)),
    ("RightUpperArm", "Spine", (-0.25, 1.13, 0.0)),
    ("RightLowerArm", "RightUpperArm", (-0.30, 0.87, 0.0)),
    ("RightHand", "RightLowerArm", (-0.31, 0.63, 0.0)),
    ("LeftUpperLeg", "Hips", (0.10, 0.74, 0.0)),
    ("LeftLowerLeg", "LeftUpperLeg", (0.10, 0.40, 0.0)),
    ("RightUpperLeg", "Hips", (-0.10, 0.74, 0.0)),
    ("RightLowerLeg", "RightUpperLeg", (-0.10, 0.40, 0.0)),
]
BONE = {n: i for i, (n, _, _) in enumerate(BONES)}
POS = {n: p for (n, _, p) in BONES}

# ---------------------------------------------------------------- mesh building
WOOD, RINGS, EYE, PUPIL, DARK, BAT, SHINE = range(7)
prims = {}

def prim(mat):
    return prims.setdefault(mat, {"p": [], "n": [], "uv": [], "j": [], "i": []})

def add_vertex(d, p, n, uv, bone):
    d["p"].append(p)
    d["n"].append(n)
    d["uv"].append(uv)
    d["j"].append(BONE[bone])
    return len(d["p"]) - 1

def norm(v):
    l = math.sqrt(v[0] ** 2 + v[1] ** 2 + v[2] ** 2) or 1.0
    return (v[0] / l, v[1] / l, v[2] / l)

def cross(a, b):
    return (a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0])

LOG_R = 0.245

def log_radius(theta, y):
    return LOG_R * (1 + 0.012 * math.sin(3 * theta + 0.4) + 0.008 * math.sin(7 * theta + 1.3))

def log_segment(y0, y1, bone, top_round=False, segs=40, rings=10):
    d = prim(WOOD)
    base = len(d["p"])
    for r in range(rings + 1):
        t = r / rings
        y = y0 + (y1 - y0) * t
        shrink = 1.0
        if top_round:  # sanded, slightly rounded top edge
            edge = max(0.0, (t - 0.86) / 0.14)
            shrink = 1 - 0.07 * edge * edge
            y = y - 0.01 * edge * edge
        for s in range(segs + 1):
            th = 2 * math.pi * s / segs
            rad = log_radius(th, y) * shrink
            x, z = math.sin(th) * rad, math.cos(th) * rad
            add_vertex(d, (x, y, z), (math.sin(th), 0.0, math.cos(th)), (s / segs, 1 - (y - 0.7) / 1.25), bone)
    for r in range(rings):
        for s in range(segs):
            a = base + r * (segs + 1) + s
            b = a + segs + 1
            d["i"] += [a, a + 1, b, b, a + 1, b + 1]
    # end grain caps
    for (y, up, shrink) in ((y0, False, 1.0), (y1 - (0.01 if top_round else 0), True, 0.93 if top_round else 1.0)):
        c = prim(RINGS)
        cb = len(c["p"])
        add_vertex(c, (0, y, 0), (0, 1 if up else -1, 0), (0.5, 0.5), bone)
        for s in range(segs + 1):
            th = 2 * math.pi * s / segs
            rad = log_radius(th, y) * shrink
            add_vertex(c, (math.sin(th) * rad, y, math.cos(th) * rad), (0, 1 if up else -1, 0),
                       (0.5 + 0.5 * math.sin(th), 0.5 + 0.5 * math.cos(th)), bone)
        for s in range(segs):
            if up:
                c["i"] += [cb, cb + 1 + s, cb + 2 + s]
            else:
                c["i"] += [cb, cb + 2 + s, cb + 1 + s]

def tube(a, b, ra, rb, mat, bone, segs=14, uv_v=(0.0, 1.0)):
    """Rounded stick from a to b (radius ra -> rb) with half-sphere ends."""
    d = prim(mat)
    axis = (b[0] - a[0], b[1] - a[1], b[2] - a[2])
    length = math.sqrt(sum(x * x for x in axis))
    w = norm(axis)
    up = (0, 0, 1) if abs(w[2]) < 0.9 else (1, 0, 0)
    u = norm(cross(w, up))
    v = cross(w, u)
    rows = []
    # profile: (offset along axis, radius, normal axial component)
    prof = []
    for k in range(5):  # bottom cap
        ang = -math.pi / 2 + (math.pi / 2) * k / 5
        prof.append((math.sin(ang) * ra, math.cos(ang) * ra, math.sin(ang)))
    for k in range(6):
        t = k / 5
        prof.append((length * t, ra + (rb - ra) * t, 0.0))
    for k in range(1, 6):  # top cap
        ang = (math.pi / 2) * k / 5
        prof.append((length + math.sin(ang) * rb, math.cos(ang) * rb, math.sin(ang)))
    base = len(d["p"])
    for ri, (off, rad, nz) in enumerate(prof):
        nr = math.sqrt(max(0.0, 1 - nz * nz))
        for s in range(segs + 1):
            th = 2 * math.pi * s / segs
            dirv = tuple(math.cos(th) * u[i] + math.sin(th) * v[i] for i in range(3))
            p = tuple(a[i] + w[i] * off + dirv[i] * rad for i in range(3))
            n = norm(tuple(dirv[i] * nr + w[i] * nz for i in range(3)))
            tt = min(1.0, max(0.0, off / max(length, 1e-6)))
            add_vertex(d, p, n, (s / segs * 0.25, uv_v[0] + (uv_v[1] - uv_v[0]) * tt), bone)
    for r in range(len(prof) - 1):
        for s in range(segs):
            i0 = base + r * (segs + 1) + s
            i1 = i0 + segs + 1
            d["i"] += [i0, i1, i0 + 1, i0 + 1, i1, i1 + 1]

def blob(c, r, mat, bone, segs=20, rings=12, rot=0.0):
    """Ellipsoid centred at c with radii r, optionally rolled around Z by rot."""
    d = prim(mat)
    base = len(d["p"])
    cr, sr = math.cos(rot), math.sin(rot)
    for i in range(rings + 1):
        phi = math.pi * i / rings
        for s in range(segs + 1):
            th = 2 * math.pi * s / segs
            nx, ny, nz = math.sin(phi) * math.sin(th), math.cos(phi), math.sin(phi) * math.cos(th)
            x, y, z = nx * r[0], ny * r[1], nz * r[2]
            x, y = x * cr - y * sr, x * sr + y * cr
            n = norm((nx / r[0], ny / r[1], nz / r[2]))
            n = (n[0] * cr - n[1] * sr, n[0] * sr + n[1] * cr, n[2])
            add_vertex(d, (c[0] + x, c[1] + y, c[2] + z), n, (s / segs * 0.2 + 0.4, i / rings * 0.2 + 0.4), bone)
    for i in range(rings):
        for s in range(segs):
            a = base + i * (segs + 1) + s
            b = a + segs + 1
            d["i"] += [a, b, a + 1, a + 1, b, b + 1]

def on_face(x, y, out):
    """Point on the front of the head log at (x, y), pushed out by `out`."""
    th = math.asin(max(-1.0, min(1.0, x / LOG_R)))
    return (x, y, math.cos(th) * log_radius(th, y) + out)

# body log (Spine) and head log (Head): one log split at the neck so the head can turn seamlessly
log_segment(0.70, 1.255, "Spine")
log_segment(1.255, 1.93, "Head", top_round=True)
# hidden neck plug: when the head tilts, the gap shows wood instead of bright end grain
tube((0.0, 1.17, 0.0), (0.0, 1.30, 0.0), LOG_R * 0.94, LOG_R * 0.94, WOOD, "Spine", segs=40)

# face
for side in (1, -1):
    ex = 0.092 * side
    ex = 0.098 * side
    blob(on_face(ex, 1.635, -0.014), (0.076, 0.09, 0.036), EYE, "Head")
    blob(on_face(ex * 0.94, 1.62, 0.017), (0.036, 0.041, 0.015), PUPIL, "Head")
    blob(on_face(ex * 0.88 + 0.013 * side, 1.638, 0.029), (0.011, 0.011, 0.005), SHINE, "Head")
    blob(on_face(ex * 1.04, 1.77, 0.004), (0.085, 0.026, 0.026), DARK, "Head", rot=0.22 * side)
blob(on_face(0.0, 1.525, -0.006), (0.046, 0.05, 0.05), WOOD, "Head")  # nose
blob(on_face(0.0, 1.40, -0.008), (0.12, 0.05, 0.024), DARK, "Head")  # open mouth
blob(on_face(0.0, 1.425, 0.008), (0.085, 0.014, 0.012), SHINE, "Head")  # upper teeth

# arms: thin sticks with round hands
for side, n in ((1, "Left"), (-1, "Right")):
    sh = (0.215 * side, 1.13, 0.0)
    el = (0.30 * side, 0.87, 0.01)
    ha = (0.31 * side, 0.64, 0.03)
    tube(sh, el, 0.046, 0.04, WOOD, n + "UpperArm")
    tube(el, ha, 0.04, 0.036, WOOD, n + "LowerArm")
    blob((ha[0], ha[1] - 0.035, ha[2]), (0.055, 0.06, 0.055), WOOD, n + "LowerArm")
    # legs: sticks with rounded feet
    hip = (0.10 * side, 0.74, 0.0)
    kn = (0.105 * side, 0.40, 0.012)
    an = (0.105 * side, 0.08, 0.0)
    tube(hip, kn, 0.062, 0.055, WOOD, n + "UpperLeg")
    tube(kn, an, 0.055, 0.05, WOOD, n + "LowerLeg")
    blob((an[0], 0.045, 0.045), (0.075, 0.045, 0.11), WOOD, n + "LowerLeg")

# the bat, held in the right hand: thin grip, knob, thick barrel
grip_top = (-0.31, 0.70, 0.02)
tip = (-0.34, 0.06, 0.36)
blob(grip_top, (0.034, 0.02, 0.034), BAT, "RightLowerArm")
d_dir = norm(tuple(tip[i] - grip_top[i] for i in range(3)))
mid = tuple(grip_top[i] + d_dir[i] * 0.3 for i in range(3))
tube(grip_top, mid, 0.022, 0.03, BAT, "RightLowerArm")
tube(mid, tip, 0.03, 0.062, BAT, "RightLowerArm")

# ---------------------------------------------------------------- glTF assembly
blob_bytes = bytearray()
views = []
accessors = []

def view(data, target=None):
    while len(blob_bytes) % 4:
        blob_bytes.append(0)
    off = len(blob_bytes)
    blob_bytes.extend(data)
    v = {"buffer": 0, "byteOffset": off, "byteLength": len(data)}
    if target:
        v["target"] = target
    views.append(v)
    return len(views) - 1

def accessor(fmt, comp, typ, rows, target=34962, minmax=False):
    flat = [x for r in rows for x in (r if isinstance(r, (tuple, list)) else (r,))]
    data = struct.pack("<%d%s" % (len(flat), fmt), *flat)
    a = {"bufferView": view(data, target), "componentType": comp, "count": len(rows), "type": typ}
    if minmax:
        a["min"] = [min(r[i] for r in rows) for i in range(3)]
        a["max"] = [max(r[i] for r in rows) for i in range(3)]
    accessors.append(a)
    return len(accessors) - 1

images = [wood_texture(), rings_texture()]
image_defs = [{"bufferView": view(img), "mimeType": "image/png"} for img in images]

def mat(name, factor, tex=None):
    m = {"name": name, "pbrMetallicRoughness": {"baseColorFactor": factor, "metallicFactor": 0.0, "roughnessFactor": 0.9}}
    if tex is not None:
        m["pbrMetallicRoughness"]["baseColorTexture"] = {"index": tex}
    return m

materials = [
    mat("wood", [1, 1, 1, 1], 0),
    mat("end_grain", [1, 1, 1, 1], 1),
    mat("eye_white", [0.95, 0.94, 0.9, 1]),
    mat("pupil", [0.0, 0.0, 0.0, 1]),
    mat("dark_wood", [0.025, 0.01, 0.005, 1]),
    mat("bat", [0.32, 0.17, 0.08, 1], 0),
    mat("shine", [1, 1, 1, 1]),
]

def fix_winding(d):
    """Every triangle wound counter-clockwise as seen from outside (matching its vertex normals)."""
    P, N, I = d["p"], d["n"], d["i"]
    for t in range(0, len(I), 3):
        a, b, c = I[t], I[t + 1], I[t + 2]
        e1 = tuple(P[b][k] - P[a][k] for k in range(3))
        e2 = tuple(P[c][k] - P[a][k] for k in range(3))
        g = cross(e1, e2)
        n = tuple(N[a][k] + N[b][k] + N[c][k] for k in range(3))
        if g[0] * n[0] + g[1] * n[1] + g[2] * n[2] < 0:
            I[t + 1], I[t + 2] = c, b

primitives = []
for m in sorted(prims):
    d = prims[m]
    fix_winding(d)
    primitives.append({
        "attributes": {
            "POSITION": accessor("f", 5126, "VEC3", d["p"], minmax=True),
            "NORMAL": accessor("f", 5126, "VEC3", d["n"]),
            "TEXCOORD_0": accessor("f", 5126, "VEC2", d["uv"]),
            "JOINTS_0": accessor("H", 5123, "VEC4", [(j, 0, 0, 0) for j in d["j"]]),
            "WEIGHTS_0": accessor("f", 5126, "VEC4", [(1.0, 0.0, 0.0, 0.0)] * len(d["j"])),
        },
        "indices": accessor("I", 5125, "SCALAR", d["i"], target=34963),
        "material": m,
    })

nodes = []
for name, parent, p in BONES:
    pp = POS[parent] if parent else (0, 0, 0)
    nodes.append({"name": name, "translation": [p[0] - pp[0], p[1] - pp[1], p[2] - pp[2]], "children": []})
for i, (name, parent, _) in enumerate(BONES):
    if parent:
        nodes[BONE[parent]]["children"].append(i)
for n in nodes:
    if not n["children"]:
        del n["children"]
mesh_node = len(nodes)
nodes.append({"name": "TungTungTungSahur", "mesh": 0, "skin": 0})

ibm = []
for name, _, p in BONES:
    ibm.append((1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, -p[0], -p[1], -p[2], 1))
ibm_acc = accessor("f", 5126, "MAT4", ibm, target=None)

gltf = {
    "asset": {"version": "2.0", "generator": "HookVisuals tung_model.py"},
    "scene": 0,
    "scenes": [{"nodes": [0, mesh_node]}],
    "nodes": nodes,
    "meshes": [{"name": "TungTungTungSahur", "primitives": primitives}],
    "skins": [{"joints": list(range(len(BONES))), "inverseBindMatrices": ibm_acc, "skeleton": 0}],
    "materials": materials,
    "samplers": [{"magFilter": 9729, "minFilter": 9987, "wrapS": 10497, "wrapT": 10497}],
    "textures": [{"source": 0, "sampler": 0}, {"source": 1, "sampler": 0}],
    "images": image_defs,
    "accessors": accessors,
    "bufferViews": views,
    "buffers": [{"byteLength": 0}],
}
while len(blob_bytes) % 4:
    blob_bytes.append(0)
gltf["buffers"][0]["byteLength"] = len(blob_bytes)
js = json.dumps(gltf, separators=(",", ":")).encode()
while len(js) % 4:
    js += b" "
out = struct.pack("<III", 0x46546C67, 2, 12 + 8 + len(js) + 8 + len(blob_bytes))
out += struct.pack("<II", len(js), 0x4E4F534A) + js
out += struct.pack("<II", len(blob_bytes), 0x004E4942) + bytes(blob_bytes)
with open(sys.argv[1] if len(sys.argv) > 1 else "tung.glb", "wb") as f:
    f.write(out)
print("vertices", sum(len(d["p"]) for d in prims.values()), "triangles", sum(len(d["i"]) for d in prims.values()) // 3, "bytes", len(out))
