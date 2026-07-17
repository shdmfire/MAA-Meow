#!/usr/bin/env python3
"""Validates META-INF/services entries and catches duplicate providers/controller IDs."""
from __future__ import annotations

import os
import re
import sys
from collections import defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PROVIDER = re.compile(r"^[A-Za-z_$][\w$]*(?:\.[A-Za-z_$][\w$]*)+$")
CONTROLLER_ID_DECLARATION = re.compile(
    r"(?:\bconst\s+val|\b(?:public\s+|private\s+|protected\s+)?static\s+final\s+String)"
    r"\s+CONTROLLER_ID\s*(?::\s*String\s*)?=\s*[\"']([^\"']+)[\"']"
)
SKIP_DIRS = {".git", ".gradle", ".idea", "build", "node_modules"}
TEST_SOURCE_DIRS = {"test", "androidTest"}


def project_files():
    for directory, dirs, files in os.walk(ROOT):
        dirs[:] = [name for name in dirs if name not in SKIP_DIRS]
        base = Path(directory)
        for name in files:
            yield base / name


def main() -> int:
    errors: list[str] = []
    providers: dict[str, list[str]] = defaultdict(list)
    all_files = list(project_files())
    for entry in all_files:
        if "META-INF/services" not in entry.as_posix():
            continue
        for number, raw in enumerate(entry.read_text(encoding="utf-8").splitlines(), 1):
            value = raw.split("#", 1)[0].strip()
            if not value:
                continue
            if not PROVIDER.fullmatch(value):
                errors.append(f"{entry.relative_to(ROOT)}:{number}: invalid provider class: {value}")
            providers[value].append(str(entry.relative_to(ROOT)))
    for provider, locations in providers.items():
        if len(locations) > 1:
            errors.append(f"duplicate provider {provider}: {', '.join(locations)}")

    ids: dict[str, list[str]] = defaultdict(list)
    for source in all_files:
        relative = source.relative_to(ROOT)
        if source.suffix not in {".kt", ".java"} or TEST_SOURCE_DIRS.intersection(relative.parts):
            continue
        for controller_id in CONTROLLER_ID_DECLARATION.findall(source.read_text(encoding="utf-8")):
            ids[controller_id].append(str(relative))
    for controller_id, locations in ids.items():
        if len(locations) > 1:
            errors.append(f"duplicate controllerId {controller_id!r}: {', '.join(locations)}")

    if errors:
        print("Service loader check failed:\n" + "\n".join(f"- {error}" for error in errors))
        return 1
    print(f"Service loader check passed ({len(providers)} provider entries, {len(ids)} controller IDs).")
    return 0


if __name__ == "__main__":
    sys.exit(main())
