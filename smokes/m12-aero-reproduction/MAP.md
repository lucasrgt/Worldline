# M12 Aero Reproduction Evidence Map

## Purpose

This cycle advances M11 from synthetic attribution and runtime loading to two
real dense-scene frame captures from the pinned Aero candidate.

## Runtime boundary

The checked Gradle init script adds one test-only Mixin source directory and
overlays only the test mod's Mixin descriptor. The hook runs from
`Minecraft.tick()` on the game thread, selects single-player mode, creates a
fixed-seed world, forces the target chunks, fixes the camera, and requests a
normal client stop after the bounded window. The Aero Git checkout must retain
its pinned origin, revision, and clean tracked status.

The hook does not enter the Worldline API, kernel, or neutral analysis module.
The Aero adapter remains the only code that knows the source logger's field
names. The runtime source is compiled by Aero's own mapped test build and is
not packaged as a Worldline product module.

## Oracle and minimization

The corrected invariant oracle requires each log to follow a readiness marker
for at least 500 live BlockEntities and contain a frame of at least 25 ms with
at least 10 ms measured inside `compileChunks`. M13 showed that a slow frame can
contain only one compile call, so expanded call count is not a universal cause.
The same stage-timing predicate must hold in two clean same-seed captures.
Frame milliseconds and record counts are deliberately not frozen.

Every stable-scene record is represented by an opaque `frame:<index>` M9
scenario step. Delta debugging must reduce the captured window to exactly one
record while preserving the predicate. This minimizes evidence selection; it
does not claim to minimize a gameplay action sequence or the upstream cause.

## Save boundary

The first created save is copied into ignored derived evidence and hashed.
M12 observed a reduced reloaded workload but did not count real entity blocks
separately from the global BlockEntity list, so the two qualifying runs recreate
the fixed seed instead of silently claiming save replay. M13 later proves that
real entity blocks persist while rejected-placement phantom entries disappear.

## Pass conditions

- both real clients reach a scene with at least 500 live BlockEntities;
- both bounded windows finish through the normal client stop path;
- both logs contain the qualifying compile-stage spike;
- both record windows minimize completely to one record;
- the invariant report matches the frozen M12 SHA-256;
- the Aero checkout remains clean and no game binary or save enters Git.

Frozen expected signature SHA-256: `804915ae89a1adef9f350adc020ed8a77986b2d3d4c1d84205009a4382ed051c`
