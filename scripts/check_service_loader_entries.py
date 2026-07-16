#!/usr/bin/env python3
"""Validates META-INF/services entries and catches duplicate providers/controller IDs."""
from __future__ import annotations

import re
import sys
from collections import defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PROVIDER = re.compile(r"^[A-Za-z_$][\w$]*(?:\.[A-Za-z_$][\w$]*)+$")
CONTROLLER_ID = re.compile(r"\bcontrollerId\s*(?::\s*String\s*)?=\s*[\"']([^\"']+)[\"']")


def main() -> int:
    errors: list[str] = []
    providers: dict[str, list[str]] = defaultdict(list)
    for entry in ROOT.rglob("META-INF/services/*"):
        if not entry.is_file() or any(part == "build" for part in entry.parts):
            continue
        for number, raw in enumerate(entry.read_text(encoding="utf-8").splitlines(), 1):
            value = raw.split("#", 1)[0].strip()
            if not value:
                continue
            if not PROVIDER.fullmatch(value):
                errors.append(f"{entry.relative_to(ROOT)}:{number}: invalid provider class: {value}")
            providers[value].append(str(entry.relative_to(ROOT)))
    for provider, files in providers.items():
        if len(files) > 1:
            errors.append(f"duplicate provider {provider}: {', '.join(files)}")

    ids: dict[str, list[str]] = defaultdict(list)
    for source in list(ROOT.rglob("*.kt")) + list(ROOT.rglob("*.java")):
        if any(part in {".git", "build"} for part in source.parts):
            continue
        for controller_id in CONTROLLER_ID.findall(source.read_text(encoding="utf-8")):
            ids[controller_id].append(str(source.relative_to(ROOT)))
    for controller_id, files in ids.items():
        if len(files) > 1:
            errors.append(f"duplicate controllerId {controller_id!r}: {', '.join(files)}")

    if errors:
        print("Service loader check failed:\n" + "\n".join(f"- {error}" for error in errors))
        return 1
    print(f"Service loader check passed ({len(providers)} provider entries, {len(ids)} controller IDs).")
    return 0


if __name__ == "__main__":
    sys.exit(main())
