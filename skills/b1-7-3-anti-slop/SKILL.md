---
name: b1-7-3-anti-slop
description: Audit and harden Minecraft Beta 1.7.3 Java mods, especially Fabric, StationAPI, Mixin, Aero rendering, storage, networking, and Worldline-integrated projects. Use when reviewing generated or handwritten b1.7.3 mod code, diagnosing client/server side leaks, validating Mixins and mappings, investigating hot-path or inventory-scan risks, installing fail-closed project checks, or promoting a reproduced Worldline failure into a reusable anti-slop rule.
---

# Beta 1.7.3 Anti-Slop

Reject plausible-looking code when its correctness is not supported by the pinned mappings, target bytecode, side boundary, or behavioral evidence.

## Workflow

1. Read repository guidance and preserve existing work.
2. Identify the exact Minecraft version, loader, StationAPI version, mappings, Java toolchain, Mixin configuration, environments, and optional integrations.
3. Classify source roots and entrypoints as common, client, server, test, or generated. Do not infer safety from class names alone.
4. Run the deterministic side-safety scan on every common and server root:

   ```text
   python skills/b1-7-3-anti-slop/scripts/audit_side_safety.py \
     --project . \
     --common-root src/main/java \
     --server-root src/server/java
   ```

   Add `--client-prefix <package-prefix>` for project-specific client-only libraries. Do not scan a mixed source root as common until entrypoint and class-closure analysis proves which files are client-only.
5. Read [rule-catalog.md](references/rule-catalog.md). Apply implemented rules as gates and use planned rules as a review checklist only.
6. For StationAPI or Mixins, read [stationapi-mixins.md](references/stationapi-mixins.md). Resolve targets against the exact pinned named JAR and verify descriptors in bytecode. Decompiled source is navigation evidence, never the behavioral oracle.
7. For storage, rendering, tick, or network performance work, read [performance-storage.md](references/performance-storage.md). Prefer structural counters over machine-time thresholds.
8. Run the narrowest relevant tests, then the repository's canonical gate. When a task reads, transforms, instruments, or executes Minecraft in Worldline, run `java tools/harness/Gate.java --runtime`; use `--smoke` for qualified vanilla behavior.
9. Report findings by rule ID, evidence, impact, and smallest safe correction. Separate confirmed violations from risks that still need runtime evidence.

## Promote Worldline Findings

Read [evidence-promotion.md](references/evidence-promotion.md) before adding or strengthening a rule. Require a minimized reproduction, causal explanation, invalid fixture, valid fixture, and deterministic failure message. Never add a prohibition solely because one implementation was slow or unfamiliar.

Prefer static enforcement for source or bytecode invariants and Worldline scenarios for behavior, concurrency, persistence, rendering, and performance. A passing static audit does not prove runtime correctness.

## Safety Boundaries

- Do not copy or distribute official JARs, original assets, decompiled Minecraft sources, or names from incompatible mapping licenses.
- Do not treat intermediary or named mappings as proof of runtime behavior.
- Do not silently rewrite build files, mappings, Mixins, or source-set layouts. Inspect first and keep installation changes reviewable.
- Do not weaken an existing test or use broad exclusions to make a rule pass.
- Do not claim a planned rule is implemented.
