# M10 Native/Offscreen Render Contract

Worldline v0.8.0 promotes one deliberately narrow native-render claim for
Minecraft Beta 1.7.3. The executable path is:

```text
Minecraft Tessellator
  -> real LWJGL 2.9.4
  -> real Windows OpenGL Pbuffer
  -> RGBA framebuffer readback
```

This is separate from the existing headless client. Headless smokes continue to
place Worldline's `Display`, `GL11`, keyboard, and mouse substitutions before
LWJGL on their classpath. The M10 smoke excludes those substitutions and uses
the hash-verified LWJGL JAR and `lwjgl64.dll` from the local RetroMCP workspace.

## Promoted guarantee

On the qualified Windows x86-64 runtime, the gate creates a current 64 by 64
Pbuffer while the real LWJGL `Display` remains uncreated. It clears the buffer,
then invokes Minecraft's singleton `Tessellator` to draw a fixed untextured
quad. Readback must contain the exact background and geometry colors, exactly
1,280 geometry pixels, no OpenGL error, and the frozen full-frame SHA-256:

```text
3f7da2d7ed9eeeff4c1ac7ad3767c82a5cb95b066cdb28bd3788e0cbcd3141ff
```

Two fresh processes use mapped `net.minecraft.src.Tessellator`; two use the
official JAR's `nw` class. The runner verifies class origin, repetition, mapped
versus official equality, and the frozen signature. The official client JAR,
mapped classes, LWJGL binaries, and generated pixels remain local.

## Frozen native inputs

The smoke descriptor pins the official client, mappings, RetroMCP version
metadata, LWJGL JAR, and 64-bit Windows LWJGL DLL by SHA-256. Runtime paths must
remain under ignored `local/`. A hardware vendor/renderer/version diagnostic is
emitted, but it is deliberately excluded from the canonical frame signature.

## Aero investigation result

The original target is known only as "Aero Model Lib" in the project brief.
No candidate JAR or source-identifying metadata exists in the repository,
`Documents`, `Downloads`, or `Desktop`; the only similarly named archive is an
unrelated Aerofortress harness. A primary-source web search did not identify an
unambiguous project artifact.

The canonical candidate location is therefore
`local/candidates/aero-model-lib.jar`, and the promoted status is
`artifact-absent`. The gate requires that path to be absent. If an artifact
appears, the gate fails until its identity, hash, loader contract, dependencies,
and runtime behavior are explicitly reviewed. The recorded compatibility is
`NOT_RUN`, never compatible or incompatible.

## Non-claims

M10 does not claim:

- a complete Minecraft world, GUI, texture, entity, or model-library frame;
- deterministic pixels across arbitrary GPUs, drivers, operating systems, or
  LWJGL versions;
- Aero Model Lib loading, compatibility, correctness, or spike reproduction;
- model-transform, matrix-operation, allocation, or frame-time attribution;
- equivalence between Pbuffer behavior and an onscreen swap chain;
- a new stable rendering API.

Those require additional artifacts and evidence. M10 establishes the smallest
real GPU boundary on which that later instrumentation can be built.
