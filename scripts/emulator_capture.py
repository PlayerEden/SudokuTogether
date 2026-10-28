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
        if wanted in (text, desc) or wanted.lower() in text.lower() or wanted.lower() in desc.lower():
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
    time.sleep(10)
    # Google API emulator images can briefly show a Pixel Launcher ANR on first
    # cold boot, over the correctly running app. Dismiss this SYSTEM dialog
    # before capturing game screens; it is unrelated to Sudoku Together.
    if tap_text("Wait"):
        time.sleep(4)
    screenshot("01-home.png")
    if tap_text("Help"):
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
    # Evidence for v0.10B: actual Daily board and locally saved Daily History.
    # ADB captures are best-effort; interaction tests separately verify navigation.
    try:
        if tap_text("Home"):
            time.sleep(1)
            tap_text("Leave room")
            time.sleep(1)
        daily_opened = False
        for title in ("Play today's challenge", "Continue today's challenge", "Review today's challenge"):
            if tap_text(title):
                daily_opened = True
                break
        if daily_opened:
            time.sleep(7)
            screenshot("06-daily-gameplay.png")
            if tap_text("Home"):
                time.sleep(1)
                tap_text("Leave room")
                time.sleep(1)
        adb("shell", "input", "swipe", "500", "1750", "500", "500", "450")
        if tap_text("My profile & records"):
            time.sleep(1)
            adb("shell", "input", "swipe", "500", "1600", "500", "750", "450")
            time.sleep(1)
            screenshot("07-daily-history-profile.png")
    except Exception as exc:
        print("Optional Daily screenshot flow could not complete:", exc, flush=True)
    print("Visual QA screenshot collection finished", flush=True)

if __name__ == "__main__":
    main()
