# Worldline v0.0.1 Completion Audit

Status: **GO**

This audit maps the first-cycle plan to executable repository evidence. The
scope ends at one controlled client tick; later capabilities are intentionally
not part of v0.0.1.

| Plan item | Evidence | Result |
| --- | --- | --- |
| Freeze exact b1.7.3 artifact and hashes | Public descriptor plus runtime SHA-1/SHA-256 verification | PASS |
| Bootstrap RetroMCP | Immutable upstream Git revision and fail-closed local bootstrap | PASS |
| Verify decompile, compile, and launch | Client was rebuilt from an absent `minecraft/` output directory; controlled client process starts | PASS |
| Inventory external boundaries | Clock, RNG, LWJGL/window, input, filesystem, networking, audio, and threading are mapped in the client smoke | PASS |
| Locate exact tick root | Machine-checked mapping: `Minecraft.runTick()V` = official client `Minecraft.k()V` | PASS |
| Expose manual tick control | Public `MinecraftRuntime.tick(int)` and bytecode-checked backend call | PASS |
| Implement `bootHeadless()` | Real `Minecraft` constructor and tick collaborators; `Display.isCreated()` remains false | PASS |
| Implement `loadWorld()` | Original client `World`, `Chunk`, and `EntityPlayerSP` camera context over in-memory persistence | PASS |
| Implement `tick(1)` | Client counter and world logical time both advance from zero to one | PASS |
| Build vanilla oracle | Separate source set compiled directly against the hash-verified official client JAR | PASS |
| Compare first controlled tick | Two mapped-client and two official-client JVMs emit the same trace and frozen signature | PASS |
| Repeat until trustworthy baseline | Pairwise determinism, cross-boundary match, frozen signature, class-origin checks, and cold client reconstruction | PASS |
| Expand only after baseline | Snapshot, rewind, replay, selectors, mods, and minimization remain outside this cycle | PASS |

## GO / NO-GO criteria

1. RetroMCP decompiles b1.7.3: **PASS**.
2. Decompiled source recompiles: **PASS**.
3. The build starts: **PASS**.
4. A world loads and ticks: **PASS**.
5. `runTick` is externally controlled: **PASS**.

The canonical command is:

```text
java tools/harness/Verify.java --smoke
```

It must finish with `verify passed` and `official client oracle: MATCH`. Exact
symbols, substitutions, non-claims, trace, and hashes are defined in
`smokes/controlled-client-tick/MAP.md`.

## Official v0.0.1 release qualification

The release-qualified tree additionally contains an authoritative
`release/worldline.properties` manifest, public `WorldlineVersion`, changelog,
roadmap, and fail-closed `ReleaseCheck`. The check cross-validates the release
version, Java target, official client digest, RetroMCP revision, and every
frozen signature against their owning descriptors. It also rejects JARs,
class files, and decompiled Minecraft trees outside the ignored local roots.

Two release-audit runs removed the complete generated `minecraft/` workspace,
then executed `java tools/harness/Verify.java --smoke`. Both runs rebuilt the
workspace with the pinned RetroMCP CLI and passed the server oracle, client
oracle, laboratory checks, source budgets, strict compilation, and tests. The
controlled mapped `Minecraft.class` SHA-256 remained:

```text
f3aba176750d89e28559b9c85b070d1819ed310b83a6703df002e768ad8ee14a
```

RetroMCP/Fernflower emitted different but behaviorally equivalent source and
bytecode shapes for one shared `World` method across cold runs. Worldline does
not claim byte-identical decompiler output. Those local outputs are not release
artifacts; equivalence is established by the hash-frozen official JAR oracle
and the unchanged canonical traces. This observed decompiler variance is also
direct evidence for the project's rule that decompiled source is not the
behavioral source of truth.
