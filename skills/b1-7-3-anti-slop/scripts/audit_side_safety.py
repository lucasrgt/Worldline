#!/usr/bin/env python3
"""Fail closed when common/server Java roots reference client-only types."""

from __future__ import annotations

import argparse
import json
import re
import sys
from dataclasses import asdict, dataclass
from pathlib import Path


DEFAULT_CLIENT_PREFIXES = (
    "net.minecraft.client.",
    "net.modificationstation.stationapi.api.client.",
    "net.modificationstation.stationapi.impl.client.",
    "org.lwjgl.",
)


@dataclass(frozen=True)
class Violation:
    rule: str
    file: str
    line: int
    prefix: str
    text: str


def arguments() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--project", default=".", help="project root")
    parser.add_argument("--common-root", action="append", default=[])
    parser.add_argument("--server-root", action="append", default=[])
    parser.add_argument("--client-prefix", action="append", default=[])
    parser.add_argument("--json", action="store_true", dest="json_output")
    return parser.parse_args()


def resolve_roots(project: Path, values: list[str]) -> list[Path]:
    roots: list[Path] = []
    for value in values:
        root = (project / value).resolve()
        if not root.is_dir():
            raise ValueError(f"source root does not exist: {root}")
        if root != project and project not in root.parents:
            raise ValueError(f"source root escapes project: {root}")
        roots.append(root)
    return roots


def scan(project: Path, roots: list[Path], prefixes: tuple[str, ...]) -> list[Violation]:
    violations: list[Violation] = []
    seen: set[Path] = set()
    for root in roots:
        for source in sorted(root.rglob("*.java")):
            source = source.resolve()
            if source in seen:
                continue
            seen.add(source)
            for number, line in enumerate(source.read_text(encoding="utf-8").splitlines(), 1):
                code = re.sub(r"//.*$", "", line)
                for prefix in prefixes:
                    if prefix in code:
                        violations.append(Violation(
                            "B173-SIDE-001",
                            source.relative_to(project).as_posix(),
                            number,
                            prefix,
                            line.strip(),
                        ))
    return violations


def main() -> int:
    args = arguments()
    project = Path(args.project).resolve()
    if not project.is_dir():
        print(f"error: project does not exist: {project}", file=sys.stderr)
        return 2
    try:
        roots = resolve_roots(project, args.common_root + args.server_root)
    except ValueError as error:
        print(f"error: {error}", file=sys.stderr)
        return 2
    if not roots:
        print("error: provide at least one --common-root or --server-root", file=sys.stderr)
        return 2
    prefixes = tuple(dict.fromkeys(DEFAULT_CLIENT_PREFIXES + tuple(args.client_prefix)))
    violations = scan(project, roots, prefixes)
    if args.json_output:
        print(json.dumps({"violations": [asdict(value) for value in violations]}, indent=2))
    else:
        for value in violations:
            print(f"{value.file}:{value.line}: {value.rule}: client-only reference "
                  f"{value.prefix!r} in common/server source")
        print(f"B173-SIDE-001: scanned {len(roots)} root(s), found {len(violations)} violation(s)")
    return 1 if violations else 0


if __name__ == "__main__":
    raise SystemExit(main())
