#!/usr/bin/env python3
"""Checks Gradle project dependencies and source imports against staged rules."""
from __future__ import annotations

import argparse
import json
import os
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PROJECT_RE = re.compile(r"\b(?:api|implementation|compileOnly|runtimeOnly|testImplementation)\s*\(\s*project\s*\(\s*[\"']([^\"']+)")
IMPORT_RE = re.compile(r"^\s*import\s+([\w.]+)", re.MULTILINE)
SKIP_DIRS = {".git", ".gradle", ".idea", "build", "node_modules"}


def project_files():
    for directory, dirs, files in os.walk(ROOT):
        dirs[:] = [name for name in dirs if name not in SKIP_DIRS]
        base = Path(directory)
        for name in files:
            yield base / name


def module_for(path: Path) -> str | None:
    rel = path.relative_to(ROOT)
    parts = rel.parts
    if len(parts) < 2 or path.name not in {"build.gradle", "build.gradle.kts"}:
        return None
    return ":" + ":".join(parts[:-1])


def matches_module(module: str, pattern: str) -> bool:
    regex = "^" + re.escape(pattern).replace(r"\*", "[^:]+") + "$"
    return re.match(regex, module) is not None


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--phase", type=int, default=0, help="rules with enabledFromPhase <= phase are enforced")
    parser.add_argument("--rules", default="scripts/module-boundary-rules.json")
    args = parser.parse_args()
    config = json.loads((ROOT / args.rules).read_text(encoding="utf-8"))
    violations: list[tuple[str, str, str, str]] = []
    all_files = list(project_files())
    build_files = [path for path in all_files if path.name == "build.gradle.kts"]

    for rule in config.get("projectDependencyRules", []):
        if rule.get("enabledFromPhase", 0) > args.phase:
            continue
        for build_file in build_files:
            source = module_for(build_file)
            if not source or not matches_module(source, rule["from"]):
                continue
            for target in PROJECT_RE.findall(build_file.read_text(encoding="utf-8")):
                if matches_module(target, rule["forbid"]):
                    violations.append((str(build_file.relative_to(ROOT)), f"project dependency: {source} -> {target}", rule["description"], rule["suggestion"]))

    for rule in config.get("importRules", []):
        if rule.get("enabledFromPhase", 0) > args.phase:
            continue
        root = ROOT / rule["sourceRoot"]
        if not root.exists():
            continue
        baseline = set(rule.get("baselineFiles", []))
        for source_file in all_files:
            if source_file.suffix not in {".kt", ".java", ".aidl"} or not source_file.is_relative_to(root):
                continue
            rel = str(source_file.relative_to(ROOT)).replace("\\", "/")
            for imported in IMPORT_RE.findall(source_file.read_text(encoding="utf-8")):
                if any(imported == prefix or imported.startswith(prefix + ".") for prefix in rule["forbiddenPrefixes"]):
                    if rel not in baseline:
                        violations.append((rel, f"import {imported}", rule["description"], rule["suggestion"]))

    if violations:
        print("Module boundary check failed:\n")
        for path, evidence, rule, suggestion in violations:
            print(f"source: {path}\nevidence: {evidence}\nrule: {rule}\nsuggested direction: {suggestion}\n")
        return 1
    print(f"Module boundary check passed (phase {args.phase}).")
    return 0


if __name__ == "__main__":
    sys.exit(main())
