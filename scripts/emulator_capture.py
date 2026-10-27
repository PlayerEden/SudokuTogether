#!/usr/bin/env python3
"""Small reproducible Android QA visual capture using ADB accessibility bounds."""
import re
import subprocess
import time
import xml.etree.ElementTree as ET
from pathlib import Path

OUT = Path("screenshots")
OUT.mkdir(exist_ok=True)

def adb(*args, capture=True):
    return subprocess.run(["adb", *args], check=True, capture_output=capture, text=capture)

def screenshot(name):
    with (OUT / name).open("wb") as output:
        subprocess.run(["adb", "exec-out", "screencap", "-p"], check=True, stdout=output)
    print("Captured", name, (OUT / name).stat().st_size, "bytes", flush=True)

def tap_text(wanted):
    adb("shell", "uiautomator", "dump", "/sdcard/ui.xml")
    adb("pull", "/sdcard/ui.xml", "/tmp/sudoku-ui.xml")
    root = ET.parse("/tmp/sudoku-ui.xml").getroot()
    for node in root.iter("node"):
        text = node.attrib.get("text", "")
        desc = node.attrib.get("content-desc", "")
        if wanted in (text, desc) or wanted.lower() in text.lower():
            bounds = re.findall(r"\d+", node.attrib.get("bounds", ""))
            if len(bounds) == 4:
                x1, y1, x2, y2 = (int(v) for v in bounds)
                adb("shell", "input", "tap", str((x1 + x2) // 2), str((y1 + y2) // 2))
                print("Tapped:", wanted, node.attrib.get("bounds"), flush=True)
                return True
    print("Could not locate UI label:", wanted, flush=True)
    (OUT / "accessibility.xml").write_bytes(Path("/tmp/sudoku-ui.xml").read_bytes())
    return False

def main():
    adb("shell", "settings", "put", "system", "font_scale", "1.0")
    adb("shell", "am", "start", "-n", "com.playereden.sudokutogether.qa/com.playereden.sudokutogether.MainActivity")
    time.sleep(8)
    screenshot("01-home.png")
    if tap_text("How to play"):
        time.sleep(1)
        screenshot("02-how-to-play.png")
        tap_text("Got it")
    if tap_text("New solo puzzle"):
        time.sleep(8)
        screenshot("03-solo-play.png")
        if tap_text("cell_40"):
            screenshot("04-selected-cell.png")
        elif tap_text("Row 5 column 5"):
            screenshot("04-selected-cell.png")
        if tap_text("?"):
            time.sleep(1)
            screenshot("05-in-game-help.png")
    print("Visual QA screenshot collection finished", flush=True)

if __name__ == "__main__":
    main()
