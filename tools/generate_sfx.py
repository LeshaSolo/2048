from __future__ import annotations

import math
import random
import wave
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
RAW = ROOT / "app" / "src" / "main" / "res" / "raw"
SAMPLE_RATE = 44_100


def clamp(value: float) -> float:
    return max(-1.0, min(1.0, value))


def write_wav(path: Path, samples: list[float]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with wave.open(str(path), "wb") as wav:
        wav.setnchannels(1)
        wav.setsampwidth(2)
        wav.setframerate(SAMPLE_RATE)
        data = bytearray()
        for sample in samples:
            value = int(clamp(sample) * 32767)
            data += value.to_bytes(2, byteorder="little", signed=True)
        wav.writeframes(data)


def envelope(t: float, attack: float, release_start: float, total: float) -> float:
    if t < attack:
        return t / attack
    if t > release_start:
        return max(0.0, (total - t) / max(0.001, total - release_start))
    return 1.0


def wooden_click() -> list[float]:
    duration = 0.105
    samples: list[float] = []
    random.seed(120)
    for index in range(int(SAMPLE_RATE * duration)):
        t = index / SAMPLE_RATE
        body = (
            math.sin(2 * math.pi * 520 * t) * math.exp(-42 * t) * 0.62
            + math.sin(2 * math.pi * 920 * t) * math.exp(-58 * t) * 0.22
            + math.sin(2 * math.pi * 1680 * t) * math.exp(-90 * t) * 0.09
        )
        tick = random.uniform(-1, 1) * math.exp(-165 * t) * 0.16
        samples.append((body + tick) * envelope(t, 0.002, 0.075, duration) * 0.62)
    return samples


def tile_swipe() -> list[float]:
    duration = 0.18
    samples: list[float] = []
    random.seed(220)
    lp = 0.0
    for index in range(int(SAMPLE_RATE * duration)):
        t = index / SAMPLE_RATE
        progress = t / duration
        scrape_noise = random.uniform(-1, 1)
        lp = lp * 0.82 + scrape_noise * 0.18
        low = math.sin(2 * math.pi * (115 + 35 * progress) * t) * 0.19
        wood_rub = lp * math.sin(math.pi * progress) * 0.34
        soft_end = math.sin(2 * math.pi * 360 * t) * math.exp(-95 * max(0, t - 0.125)) * (1 if t > 0.125 else 0) * 0.08
        samples.append((low + wood_rub + soft_end) * envelope(t, 0.008, 0.145, duration) * 0.58)
    return samples


def main() -> None:
    write_wav(RAW / "sfx_button_click.wav", wooden_click())
    write_wav(RAW / "sfx_tile_swipe.wav", tile_swipe())


if __name__ == "__main__":
    main()
