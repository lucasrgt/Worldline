# M10 Native Render Evidence Map

Frozen expected signature SHA-256: 3f7da2d7ed9eeeff4c1ac7ad3767c82a5cb95b066cdb28bd3788e0cbcd3141ff

## Purpose

This smoke proves a real offscreen rendering path without changing or
reclassifying the existing headless client path.

## Frozen symbols

`symbols.map` records the mapped and official names for the b1.7.3
`Tessellator` singleton and the four operations used by the scenario:
start-quads, set RGBA color, add vertex, and draw. The frozen mappings file and
official JAR are independently hash-verified before execution.

## Runtime path

```text
NativeRenderSmoke
  -> mapped Tessellator or official nw
  -> lwjgl-2.9.4-nightly-20150209.jar
  -> lwjgl64.dll
  -> OpenGL Pbuffer 64x64
  -> glReadPixels RGBA
```

Headless stub classes are absent from this classpath. The scenario requires a
current Pbuffer and `Display.isCreated() == false` before accepting evidence.

## Differential layout

The runner starts four fresh JVMs:

1. mapped renderer process A;
2. mapped renderer process B;
3. official renderer process A;
4. official renderer process B.

Each process validates two sentinel pixels, counts all geometry-colored pixels,
checks `glGetError`, and hashes the complete 16,384-byte RGBA buffer. The runner
checks repetition, cross-boundary equality, code-source origin, and the frozen
hash. GPU identity is diagnostic only.

## Aero boundary

The candidate path is `local/candidates/aero-model-lib.jar`. The descriptor
declares `artifact-absent`, and the runner fails if a file appears there without
an explicit qualification update. This smoke does not load or emulate Aero
Model Lib and does not claim compatibility.

## Pass conditions

- every pinned runtime input matches its SHA-256;
- the platform is Windows with a 64-bit JVM;
- the Pbuffer is supported and current in all four processes;
- no onscreen `Display` is created;
- geometry coverage is exactly 1,280 pixels;
- mapped and official frame hashes agree across all processes;
- the frame hash equals the committed signature;
- the mapped and official renderer origins are correct;
- the Aero candidate remains absent under the declared status.
