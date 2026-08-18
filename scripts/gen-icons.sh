#!/usr/bin/env bash
# Regenerates every launcher-icon and splash resource from a single source
# image, so assets/ and app/src/main/res/ can never drift apart again.
#
#   ./scripts/gen-icons.sh [source.png] [nearest|lanczos]
#
# Source should be a square PNG with transparency, 1024x1024 or larger.
#
# SPLASH_FILL, FG_FILL and LEGACY_FILL override how much of each canvas the
# mark occupies. Android 12+ shows only the middle 2/3 of the splash icon (the
# inner 192dp of a 288dp box) when no windowSplashScreenIconBackgroundColor is
# set, so SPLASH_FILL above 0.66 has its edges cut off; drop to ~0.47 if the
# device masks that window to a circle and clips the corners of square art.
set -euo pipefail

cd "$(dirname "$0")/.."

SRC="${1:-assets/sudoku_icon.png}"
RESAMPLE="${2:-lanczos}"
SPLASH_FILL="${SPLASH_FILL:-0.62}"
FG_FILL="${FG_FILL:-0.55}"
LEGACY_FILL="${LEGACY_FILL:-0.76}"
RES="${RES:-app/src/main/res}"

[ -f "$SRC" ] || { echo "no such source image: $SRC" >&2; exit 1; }

python3 - "$SRC" "$RES" "$RESAMPLE" "$SPLASH_FILL" "$FG_FILL" "$LEGACY_FILL" <<'PY'
import sys
from PIL import Image, ImageChops

src_path, res, resample_name = sys.argv[1], sys.argv[2], sys.argv[3]
splash_fill, fg_fill, legacy_fill = (float(a) for a in sys.argv[4:7])
resample = {"nearest": Image.NEAREST, "lanczos": Image.LANCZOS}[resample_name]

src = Image.open(src_path).convert("RGBA")
if src.width != src.height:
    print(f"warning: source is {src.width}x{src.height}, not square", file=sys.stderr)

# The ink mask - anything that is both opaque and not white. Framing is measured
# against this rather than the file's bounds, so however much empty margin the
# source export happened to include does not change the result.
alpha = src.getchannel("A")
not_white = ImageChops.difference(
    src.convert("RGB"), Image.new("RGB", src.size, (255, 255, 255))
).convert("L").point(lambda v: 255 if v > 16 else 0)
ink = ImageChops.multiply(not_white, alpha.point(lambda v: 255 if v > 16 else 0))

if not ink.getbbox():
    print("source image is blank", file=sys.stderr)
    raise SystemExit(1)

# An export with an opaque white backdrop would paint a white square over the
# adaptive icon's background layer. For the monochrome mark this app uses, the
# backdrop can be keyed out by luminance, which keeps the antialiased edges.
if alpha.getextrema()[0] == 255:
    levels = src.convert("L").getextrema()
    if levels[0] < 32 and levels[1] > 223:
        src.putalpha(ImageChops.invert(src.convert("L")))
        src = Image.merge("RGBA", (*Image.new("RGB", src.size).split(), src.getchannel("A")))
    else:
        print(
            "warning: source is fully opaque and not monochrome - the adaptive "
            "foreground will carry its background. Export with transparency.",
            file=sys.stderr,
        )

art = src.crop(ink.getbbox())


def render(size, fill, background):
    """Center the art at `fill` of a `size` canvas over `background`."""
    inner = max(1, round(size * fill))
    scaled = art.copy()
    scaled.thumbnail((inner, inner), resample)
    canvas = Image.new("RGBA", (size, size), background)
    canvas.alpha_composite(
        scaled, ((size - scaled.width) // 2, (size - scaled.height) // 2)
    )
    return canvas


# Densities as multiples of the mdpi baseline.
DENSITIES = {"mdpi": 1, "hdpi": 1.5, "xhdpi": 2, "xxhdpi": 3, "xxxhdpi": 4}
WHITE = (255, 255, 255, 255)
CLEAR = (255, 255, 255, 0)

written = []
for name, scale in DENSITIES.items():
    d = f"{res}/mipmap-{name}"

    # Legacy square icon: 48dp, unmasked, so it can carry the mark large.
    out = f"{d}/ic_launcher.png"
    render(round(48 * scale), legacy_fill, WHITE).convert("RGB").save(out)
    written.append(out)

    # Adaptive foreground: 108dp canvas. Launchers mask this to their own
    # shape; only a 66dp circle is guaranteed to survive. Square art with
    # content in its corners has to come down to ~0.43 to clear that circle.
    out = f"{d}/ic_launcher_foreground.png"
    render(round(108 * scale), fg_fill, CLEAR).save(out)
    written.append(out)

# Android 12+ splash icon. The platform scales this drawable to a fixed dp
# size, so the pixel dimensions do not affect how large the mark appears -
# only the share of the canvas it covers does.
out = f"{res}/drawable-nodpi/sudoku_splash.png"
if splash_fill > 0.66:
    print(
        f"warning: SPLASH_FILL={splash_fill} exceeds the 0.66 visible area - "
        "the splash will clip the edges of the mark",
        file=sys.stderr,
    )
render(512, splash_fill, CLEAR).save(out)
written.append(out)

for path in written:
    im = Image.open(path)
    print(f"  {path}  {im.width}x{im.height}")
print(f"\n{len(written)} files regenerated from {src_path} ({src.width}x{src.height}, {resample_name})")
PY
