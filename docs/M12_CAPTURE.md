# M12 Aero Runtime Capture

M12 executes the pinned Aero Model Lib 3.0.0 test consumer in the real
Fabric Loader, StationAPI, Minecraft, LWJGL, and OpenGL client. A test-only
Mixin is added through a Gradle init script; no tracked Aero source is changed.

## Controlled scene

Each capture creates `WorldlineAero` from seed `17320110707`, synchronously
loads a 3 by 3 chunk area around chunk zero, and waits for at least 500 live
BlockEntities before measurement. During the 240 measured ticks the player is
held at `(8.5, 67, 8.5)` with zero velocity and a fixed yaw and pitch. This
removes menu automation, spawn position, falling, and chunk-load timing from
the input while retaining the real client render loop.

The Aero spike logger records frame duration, GC time, render stages, chunk
compilation, batching, cell rendering, culling, display-list use, and visible
chunks. Derived raw logs and the created save remain under `.worldline/` and
are not release artifacts.

## Reproduced result

Two clean worlds created from the same controlled input both contained dense
Aero work and both produced frames above 25 ms whose expanded named work was
chunk compilation. Representative frames from the qualification run were:

| Capture | Frame | Chunk compile | Maximum compile | GC pause |
| --- | ---: | ---: | ---: | ---: |
| 1 | 45.7 ms | 36.3 ms | 34.3 ms | 0 ms |
| 2 | 61.8 ms | 50.7 ms | 50.7 ms | 6 ms |

The neutral M11 attribution model classifies the recurring bounded-fixture
spike as `LOGICAL_WORK` with `chunks.compiled` as its top expanded counter.
The M9 minimizer independently reduces each captured record window to one
frame that preserves that exact predicate.

## Persistence finding and non-claims

The generated save is captured and hashed, but the current Aero test fixture's
custom MEGA BlockEntities do not survive a fresh save reload. World generation
prints `Attempted to place a tile entity where there was no entity tile`, and
the reloaded scene loses the dense model workload. M12 therefore recreates the
same seed and camera twice instead of claiming byte-snapshot replay.

M12 proves a repeatable chunk-compilation spike in the controlled MEGA fixture.
It does not prove that this is the historical user-reported random spike, that
all Aero scenes have the same cause, or that frame durations are deterministic
across machines. Save persistence and a no-Aero/empty-scene differential are
follow-up experiments.
