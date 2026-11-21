#!/usr/bin/env python3
"""Validate and stage eight candidate collection sprites. Dry-run by default.

Run on an isolated Beta 2 review branch, from the repository root:
  python -m pip install Pillow
  python scripts/stage_beta2_last_eight.py --zip "/path/to/GridMates_Beta2_Ocean_Woodland_8Icon_ReviewPack.zip"
  python scripts/stage_beta2_last_eight.py --zip "/path/to/GridMates_Beta2_Ocean_Woodland_8Icon_ReviewPack.zip" --apply

This does not edit Kotlin, catalog definitions, original cosmetics, saves, signing or LAN.
It does not run Android tests, merge a branch or release Beta 2.
"""
from __future__ import annotations

import argparse
import hashlib
import io
import json
from pathlib import Path
from zipfile import ZipFile
from PIL import Image

MANIFEST = Path("design/beta2-content-pack/ocean-woodland-eight-review/manifest.json")
RES = Path("app/src/main/res/drawable-nodpi")
ALLOWED = {
    "ocean_seahorse", "ocean_dolphin", "ocean_seashell", "ocean_clownfish",
    "woodland_chipmunk", "woodland_beaver", "woodland_woodpecker", "woodland_skunk",
}


def sha256(blob: bytes) -> str:
    return hashlib.sha256(blob).hexdigest()


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--zip", type=Path, required=True)
    parser.add_argument("--repo-root", type=Path, default=Path("."))
    parser.add_argument("--apply", action="store_true",
                        help="Copy after all checks pass; never overwrite different existing art")
    args = parser.parse_args()
    root = args.repo_root.resolve()
    stored = json.loads((root / MANIFEST).read_text(encoding="utf-8"))
    records = stored["items"]
    ids = [item["id"] for item in records]
    if len(records) != 8 or len(set(ids)) != 8 or set(ids) != ALLOWED:
        raise ValueError("Incorrect manifest roster; aborting")
    if not (root / RES).is_dir():
        raise ValueError(f"Missing Android drawable folder: {root / RES}")
    writes = []
    with ZipFile(args.zip) as archive:
        # Require ZIP to match pinned GitHub manifest, not only its own metadata.
        for item in records:
            asset_id = item["id"]
            name = f"candidate_icons/{asset_id}.png"
            data = archive.read(name)
            if sha256(data) != item["sha256"]:
                raise ValueError(f"{asset_id}: candidate bytes differ from pinned SHA256")
            with Image.open(io.BytesIO(data)) as image:
                if image.format != "PNG" or image.mode != "RGBA" or image.size != (512, 512):
                    raise ValueError(f"{asset_id}: expected 512x512 RGBA PNG")
                alpha = image.getchannel("A")
                if alpha.getbbox() is None or alpha.getextrema()[0] != 0:
                    raise ValueError(f"{asset_id}: missing art or true transparent pixels")
                if any(alpha.getpixel(point) > 12 for point in
                       ((0,0), (511,0), (0,511), (511,511), (256,0), (0,256),
                        (511,256), (256,511))):
                    raise ValueError(f"{asset_id}: touches canvas edge")
            dest = root / RES / (asset_id + ".png")
            if dest.exists():
                if sha256(dest.read_bytes()) != sha256(data):
                    raise FileExistsError(f"Existing different sprite at {dest}; refusing overwrite")
                status = "already exact"
            else:
                status = "new file"
            writes.append((dest, data, status))
        # No writes until all source, format and destination collision checks pass.
        for dest, data, status in writes:
            if args.apply and status == "new file":
                dest.write_bytes(data)
            print(f"{dest.name}: {status}" + (" (staged)" if args.apply else " (dry run)"))
    print(f"Verified {len(writes)}/8 pinned PNGs. "
          f"New copies needed: {sum(status == 'new file' for _,_,status in writes)}. "
          "No catalog or gameplay changes.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
