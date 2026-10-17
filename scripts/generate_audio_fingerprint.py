#!/usr/bin/env python3
"""Regenerate the bundled fingerprint module with WABT 1.0.42 (wasm2c on PATH)."""

import base64
import hashlib
from pathlib import Path
import re
import shutil
import subprocess
import tempfile
import urllib.request

ROOT = Path(__file__).resolve().parents[1]
DEST = ROOT / "app/src/main/cpp/fingerprint"
REVISION = "2db6684453297ee86dec0492842f2320b08182d9"
UPSTREAM = f"https://raw.githubusercontent.com/NeteaseCloudMusicApiEnhanced/api-enhanced/{REVISION}/"
WABT = "https://raw.githubusercontent.com/WebAssembly/wabt/1.0.42/"


def download(url):
    with urllib.request.urlopen(url, timeout=60) as response:
        return response.read()


def normalize(source):
    return b"\n".join(line.rstrip(b" \t\r") for line in source.split(b"\n"))


def main():
    tool = shutil.which("wasm2c")
    if not tool or subprocess.check_output([tool, "--version"], text=True).strip() != "1.0.42":
        raise SystemExit("Place WABT 1.0.42 wasm2c on PATH before running this script.")
    source = download(UPSTREAM + "public/audio_match_demo/afp.wasm.js")
    assert hashlib.sha256(source).hexdigest() == "4926a6d69527a1afbc7f7d120b9c21947e677d80c484c8b65572fa7d4ce3b99f"
    binary = base64.b64decode(re.search(rb"['\"]([A-Za-z0-9+/=]{100,})['\"]", source)[1], validate=True)
    DEST.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory() as temp:
        wasm = Path(temp) / "afp.wasm"
        wasm.write_bytes(binary)
        subprocess.run([tool, str(wasm), "--module-name=afp", "-o", str(DEST / "afp.c")], check=True)
    for name in ["afp.c", "afp.h"]:
        path = DEST / name
        path.write_bytes(normalize(path.read_bytes()))
    runtime = DEST / "runtime"
    runtime.mkdir(exist_ok=True)
    for name in ["wasm-rt.h", "wasm-rt-impl.c", "wasm-rt-impl.h", "wasm-rt-mem-impl.c", "wasm-rt-mem-impl-helper.inc", "wasm-rt-impl-tableops.inc", "wasm-rt-exceptions.h", "wasm-rt-exceptions-impl.c"]:
        (runtime / name).write_bytes(normalize(download(WABT + "wasm2c/" + name)))
    (DEST / "LICENSE.algorithm").write_bytes(download(UPSTREAM + "LICENSE"))
    (DEST / "LICENSE.wabt").write_bytes(download(WABT + "LICENSE"))
    print("Generated pinned fingerprint module and runtime support.")


if __name__ == "__main__":
    main()
