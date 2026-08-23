# M10 Native/Offscreen Render GO Audit

Worldline v0.8.0 qualifies the first real offscreen OpenGL rendering path.

## Result

Status: **GO** for the bounded native/offscreen render contract.

Aero investigation status: **ARTIFACT ABSENT / RUNTIME COMPATIBILITY NOT RUN**.

## Canonical command

```text
java tools/harness/Gate.java --smoke
```

The native slice can also be run directly after the mapped client workspace is
prepared:

```text
java tools/smoke/NativeRenderCycle.java m10-native-render
```

## Executed evidence

- platform: Windows x86-64;
- native context: real LWJGL `Pbuffer`, current in every process;
- onscreen display: not created;
- renderer root: Minecraft `Tessellator`;
- processes: two mapped plus two official-JAR oracle processes;
- geometry coverage: 1,280 exact RGBA pixels;
- mapped repetition: match;
- official repetition: match;
- mapped versus official: match;
- frozen frame SHA-256:
  `3f7da2d7ed9eeeff4c1ac7ad3767c82a5cb95b066cdb28bd3788e0cbcd3141ff`.

Generated evidence is written to
`.worldline/smokes/m10-native-render/evidence.txt`. It records the context,
display state, render path, oracle result, frame hash, and explicit Aero
absence/non-execution result.

## Boundary audit

The native smoke has no headless LWJGL stubs on its compile or runtime
classpath. Existing client and lab cycles are unchanged and continue to use
their headless substitutions. The runner verifies the official JAR, mappings,
LWJGL JAR, and native DLL before process creation and checks whether the loaded
renderer came from mapped output or the official JAR as appropriate.

No JAR, DLL, decompiled source, game asset, framebuffer, or Aero code is
committed. The public tree contains only original Worldline source, symbol
metadata, hashes, and documentation.

## Cold qualification

The canonical gate passed both with a prepared runtime and from an empty
b1.7.3 workspace. The cold run executed RetroMCP setup, decompiled and
recompiled both sides, rebuilt every adapter/scenario, and preserved all M1-M10
signatures. The reconstructed mapped classes retained SHA-256 values
`f3aba176750d89e28559b9c85b070d1819ed310b83a6703df002e768ad8ee14a`
for `Minecraft.class` and
`66320e0306ad266d3bcd73e3b16417a277265c8c44bcbf69615557fb1554ca61`
for `Tessellator.class`.

## Promotion decision

The real native boundary, official differential oracle, frozen output, and
fail-closed provenance checks satisfy the M10 render objective. The missing
Aero artifact is not converted into a compatibility claim; it is a named,
executable absence result. Runtime Aero qualification remains future work that
starts only when the exact user-owned candidate is supplied.
