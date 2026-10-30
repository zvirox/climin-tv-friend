#!/usr/bin/env python3
"""Send photos from this computer to the TV, for Your UI's screensaver and home backdrop.

Usage:
    TV_HOST=192.168.1.50 tools/venv/bin/python -I tools/photos.py ~/Pictures/holiday/*.jpg
    TV_HOST=192.168.1.50 tools/venv/bin/python -I tools/photos.py -r ~/Pictures/holiday
    TV_HOST=192.168.1.50 tools/venv/bin/python -I tools/photos.py --via inbox beach.jpg

Options:
    --package PKG   the installed launcher (default io.yourtv.home; use io.yourtv.home.debug
                    for a debug install)
    --via PATH      auto (default), direct or inbox
    --keep-names    keep the files' own names instead of a send-time prefix
    --dry-run       say what would be sent, without connecting
    --force         send photos the app will skip (smaller than 640 px on the short side)

How a photo gets there
    1. direct: adb writes the app's own external folder,
       /sdcard/Android/data/<pkg>/files/photos. No permission is needed and nothing is imported
       through the app for it.
    2. inbox: where Android 11+ (and Fire OS 8) keeps adb out of that folder, the file is pushed to
       /data/local/tmp and streamed into PhotoInbox, the app's content provider:
       content write --uri content://io.yourtv.home.photos/<name>?size=<bytes>
       Only adb's shell may write there; the provider keeps a partial send out of the way and
       renames the file only when all of it arrived.

With auto, the direct path is tried first and the inbox is used from the first file adb cannot
write, for the rest of the run.

What happens next
    The launcher imports what is waiting the next time the home screen, the screensaver or Settings
    opens (HomeView/Photos.importInbox). It turns each photo upright, crops it to 16:9, re-encodes it
    at 2112x1188 as JPEG (which also drops EXIF and GPS) and keeps at most 100. Open Home Settings
    once after sending if the home screen is already on.
"""
import argparse
import os
import re
import sys
import time

# The content provider's authority is fixed in the manifest (`android:authorities`), the same in a
# debug and a release build; --package only changes where the direct path writes.
AUTHORITY = "io.yourtv.home.photos"
# Photos.image() accepts these; .heic/.heif only where the TV can decode them (API 28+).
EXTENSIONS = (".jpg", ".jpeg", ".png", ".webp", ".heic", ".heif")
SOF = {0xC0, 0xC1, 0xC2, 0xC3, 0xC5, 0xC6, 0xC7, 0xC9, 0xCA, 0xCB, 0xCD, 0xCE, 0xCF}
MIN_SIDE = 640   # Photos.MIN_SIDE: smaller ones are icons and thumbnails, not photos
TMP_DIR = "/data/local/tmp"
SAFE = re.compile(r"[^A-Za-z0-9._-]+")


def parse_args(argv):
    ap = argparse.ArgumentParser(
        prog="photos.py", description="Send photos to Your UI on the TV.",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""\
How a photo gets there
  1. direct: adb writes the app's own external folder,
     /sdcard/Android/data/<pkg>/files/photos.
  2. inbox: where Android 11+ (and Fire OS 8) keeps adb out of that folder, the file is pushed to
     /data/local/tmp and streamed into PhotoInbox, the app's content provider
     (content://io.yourtv.home.photos/<name>?size=<bytes>). Only adb's shell may write there, and
     the provider renames the file only when all of it arrived.

With --via auto the direct folder is tried first; the inbox is used from the first file adb cannot
write, for the rest of the run. The launcher imports what is waiting the next time the home screen,
the screensaver or Settings opens: 16:9, 2112x1188 JPEG, at most 100.""")
    ap.add_argument("paths", nargs="+", help="photos, or folders of them")
    ap.add_argument("-r", "--recursive", action="store_true", help="look inside folders, too")
    ap.add_argument("--package", default="io.yourtv.home", help="installed launcher package")
    ap.add_argument("--via", choices=("auto", "direct", "inbox"), default="auto",
                    help="how to get the file to the app (default auto)")
    ap.add_argument("--keep-names", action="store_true",
                    help="keep the original names (the app shows photos in name order)")
    ap.add_argument("--dry-run", action="store_true", help="only say what would be sent")
    ap.add_argument("--force", action="store_true", help="send photos the app would skip")
    return ap.parse_args(argv)


def photos_in(paths, recursive):
    """The photo files under the given files and folders, in the order given (folders in name order)."""
    out, missed = [], []
    for given in paths:
        if os.path.isdir(given):
            found = []
            for root, dirs, files in os.walk(given) if recursive else [(given, [], os.listdir(given))]:
                dirs.sort()
                for name in sorted(files):
                    path = os.path.join(root, name)
                    if os.path.isfile(path):
                        found.append(path)
            out.extend(found)
        elif os.path.isfile(given):
            out.append(given)
        else:
            missed.append(given)
    return out, missed


def jpeg_size(f):
    """(width, height) from a JPEG's SOF marker, or None while the file is not a readable JPEG."""
    f.seek(2)
    while True:
        byte = f.read(1)
        while byte and byte != b"\xff":
            byte = f.read(1)          # padding between segments
        while byte == b"\xff":
            byte = f.read(1)          # fill bytes
        if not byte:
            return None
        marker = byte[0]
        if marker in (0x01, 0xD8) or 0xD0 <= marker <= 0xD7:
            continue                  # no length field
        if marker in (0xD9, 0xDA):
            return None               # end of image, or the scan began: no SOF before it
        length = f.read(2)
        if len(length) < 2:
            return None
        size = int.from_bytes(length, "big")
        if size < 2:
            return None
        if marker in SOF:
            body = f.read(5)
            if len(body) < 5:
                return None
            return int.from_bytes(body[3:5], "big"), int.from_bytes(body[1:3], "big")
        f.seek(size - 2, os.SEEK_CUR)


def dimensions(path):
    """(width, height) read from the file's own header, or None when the format is not one we can
    read here (HEIC) or the file is not an image."""
    try:
        with open(path, "rb") as f:
            head = f.read(32)
            if head[:2] == b"\xff\xd8":
                return jpeg_size(f)
            if head[:8] == b"\x89PNG\r\n\x1a\n" and head[12:16] == b"IHDR":
                return int.from_bytes(head[16:20], "big"), int.from_bytes(head[20:24], "big")
            if head[:4] == b"RIFF" and head[8:12] == b"WEBP":
                kind = head[12:16]
                if kind == b"VP8X":
                    w = 1 + int.from_bytes(head[24:27], "little")
                    h = 1 + int.from_bytes(head[27:30], "little")
                    return w, h
                if kind == b"VP8 ":
                    f.seek(26)
                    start = f.read(4)
                    if start[3:] != b"\x9d\x01\x2a":
                        return None
                    more = f.read(4)
                    return (int.from_bytes(more[0:2], "little") & 0x3FFF,
                            int.from_bytes(more[2:4], "little") & 0x3FFF)
                if kind == b"VP8L":
                    f.seek(21)
                    b = f.read(4)
                    return (1 + (((b[1] & 0x3F) << 8) | b[0]),
                            1 + (((b[3] & 0x0F) << 10) | (b[2] << 2) | ((b[1] & 0xC0) >> 6)))
    except OSError:
        return None
    return None


def safe_name(path, keep_names, sequence):
    """The name the photo is queued under. The app sorts the queue by name, so a send-time prefix is
    what makes several sends show in the order they were sent; the provider rejects names with a
    slash or a leading dot, and Photos only imports the extensions it knows."""
    base = SAFE.sub("-", os.path.basename(path)).strip("-") or "photo"
    if base.startswith("."):
        base = base.lstrip(".")
    stem, ext = os.path.splitext(base)
    ext = ext.lower() or ".jpg"
    if ext not in EXTENSIONS:
        return None
    if keep_names:
        return stem + ext
    return time.strftime("%Y%m%d-%H%M%S-") + f"{sequence:03d}-" + stem + ext


def q(value):
    """Single-quoted for the TV's shell. Every name that reaches this point went through SAFE."""
    return "'" + str(value) + "'"


def package_installed(dev, package):
    # Exact lines: `pm list packages io.yourtv.home` also matches a debug build, and the direct
    # path would then write the release folder that the debug build never reads.
    out = dev.shell(f"pm list packages {q(package)}", read_timeout_s=30.0)
    return f"package:{package}" in out.splitlines()


def external_dir(package):
    # getExternalFilesDir("photos") on the TV, as adb writes it.
    return f"/sdcard/Android/data/{package}/files/photos"


def push_direct(dev, send, folder, local, name):
    """Copy the photo into the app's own external folder. Returns None on success, or the reason."""
    try:
        dev.shell(f"mkdir -p {q(folder)}", read_timeout_s=30.0)
        # The chunked transfer, not adb's sync push: some TVs (this Xiaomi/MiTV among them) never
        # answer a sync write, and send() verifies the copy with md5sum before it reaches the folder.
        result = send(dev, local, f"{folder}/{name}")
    except Exception as error:      # no external storage, a timeout, a dropped connection
        return str(error) or error.__class__.__name__
    return None if result.ok else result.error


def push_inbox(dev, send, local, name, size):
    """Stream the photo into PhotoInbox. Returns None on success, or the reason."""
    tmp = f"{TMP_DIR}/yourui-photo-{os.getpid()}-{name}"
    uri = f"content://{AUTHORITY}/{name}?size={size}"
    try:
        result = send(dev, local, tmp)
        if not result.ok:
            return result.error
        out = dev.shell(f"content write --user 0 --uri {q(uri)} < {q(tmp)}; echo rc=$?",
                        read_timeout_s=300.0)
    except Exception as error:
        return str(error) or error.__class__.__name__
    finally:
        try:
            dev.shell(f"rm -f {q(tmp)}", read_timeout_s=30.0)
        except Exception:
            pass                # a disconnected TV may refuse cleanup; the send's own error matters more
    rc = next((int(line[3:]) for line in out.splitlines() if line.startswith("rc=")), None)
    if rc != 0:
        detail = " ".join(line.strip() for line in out.splitlines()
                          if line.strip() and not line.startswith("rc=")) or "no output"
        return f"content write failed (exit {rc}): {detail}"
    return None


def main(argv):
    args = parse_args(argv)
    files, missed = photos_in(args.paths, args.recursive)
    for path in missed:
        print(f"not found: {path}", file=sys.stderr)

    plan, skipped = [], []   # (path, name, size, (w, h)) and (path, why, force can send it)
    for path in files:
        if os.path.splitext(path)[1].lower() not in EXTENSIONS:
            skipped.append((path, "not a format the launcher imports (JPEG, PNG, WebP, HEIC)", False))
            continue
        size = os.path.getsize(path)
        if size == 0:
            skipped.append((path, "the file is empty", False))
            continue
        w_h = dimensions(path)
        if w_h and min(w_h) < MIN_SIDE:
            skipped.append((path, f"{w_h[0]}x{w_h[1]} is smaller than the {MIN_SIDE} px the app keeps", True))
            continue
        name = safe_name(path, args.keep_names, len(plan) + 1)   # plan order is the order sent
        plan.append((path, name, size, w_h))

    for path, why, forceable in skipped:
        hint = "" if args.force or not forceable else " (--force sends it anyway)"
        print(f"skip {path}: {why}{hint}")
    if args.force:
        keep_skipped = []
        for path, why, forceable in skipped:
            if not forceable:
                keep_skipped.append((path, why, forceable))
                continue
            name = safe_name(path, args.keep_names, len(plan) + 1)
            plan.append((path, name, os.path.getsize(path), None))
            print(f"forced {path} as {name}")
        skipped = keep_skipped

    if not plan:
        print("nothing to send")
        return 0 if not missed and not skipped else 1
    if args.dry_run:
        for path, name, size, w_h in plan:
            dim = f"{w_h[0]}x{w_h[1]}" if w_h else "unreadable size"
            print(f"would send {path} ({size} bytes, {dim}) as {name}")
        return 0

    # Imported only now: tv.py exits at import time when TV_HOST is unset, which would hide the
    # messages above.
    sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
    import tv  # noqa: E402
    import b64push  # noqa: E402

    dev = tv.connect()
    failed = 0
    try:
        if not package_installed(dev, args.package):
            print(f"{args.package} is not installed on the TV (--package names another build)",
                  file=sys.stderr)
            return 1
        via = args.via
        folder = external_dir(args.package)
        for path, name, size, _ in plan:
            if via == "direct" or via == "auto":
                why = push_direct(dev, b64push.send, folder, path, name)
                if why is None:
                    print(f"sent {path} -> {folder}/{name} ({size} bytes)")
                    continue
                if via == "direct":
                    print(f"failed {path}: {why}", file=sys.stderr)
                    failed += 1
                    continue
                print(f"the direct folder is not writable ({why.splitlines()[0] if why else 'refused'}); "
                      "using the photo inbox for the rest", file=sys.stderr)
                via = "inbox"
            why = push_inbox(dev, b64push.send, path, name, size)
            if why is None:
                print(f"sent {path} -> photo inbox as {name} ({size} bytes)")
            else:
                print(f"failed {path}: {why}", file=sys.stderr)
                failed += 1
    finally:
        dev.close()

    sent = len(plan) - failed
    if sent:
        print(f"\n{sent} photo(s) sent. The launcher imports them the next time the home screen, the "
              "screensaver or Settings opens; open Settings once if the home screen is already on. "
              "It keeps up to 100 and shows them in name order.")
    return 0 if failed == 0 and not missed and not skipped else 1


if __name__ == "__main__":
    try:
        sys.exit(main(sys.argv[1:]))
    except KeyboardInterrupt:
        sys.exit(130)
