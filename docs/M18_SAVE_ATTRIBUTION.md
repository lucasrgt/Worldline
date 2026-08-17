# M18 Save Attribution

M18 reopens the historical random-spike question that M17 could not answer.
Every prior Aero capture set `-Daero.benchmark.skipNonForcedSaves=true`, so
autosave and block-entity serialization were absent from the timeline.

## What is now visible

The capture Gradle property defaults to the historical skip for M12-M17. M18
turns that skip off for one of two restored dense saves and injects one
non-forced world save at a known tick. Aero's existing frame logger already
emits `worldSaveMs`, `worldSaveAllocMB`, `worldSaveSkipped`, `heap`,
`gcTimeDeltaMs`, and `compileChunksMs` on the same line.

The skipped twin must cancel that save. The live twin must show a nonzero
save duration beside compile, GC, heap, and allocation counters. That is the
attribution boundary: a save is no longer an invisible periodic hitch.

On the release-gate dense fixture the live save can dominate its own frame
while `compileChunks` is idle, and a larger compile-dominated frame can still
exist in the same window. Those magnitudes stay observational.

## What remains a non-claim

A synthetic dense fixture is not the user's machine-dense map. Measuring one
injected save does not prove that every historical stall was a save, a GC
pause after that save, or a single oversized chunk rebuild. The adaptive
scheduler stays lab-only NO-GO. The M16 framebuffer threshold is unchanged.

The lived historical spike is also a different window: it appears at random
intervals after the world has already finished loading. Vanilla b1.7.3
explains that shape. `World.autosavePeriod` is 40 ticks, so a non-forced
`saveWorld(false)` runs about every two seconds. That call writes at most
24 dirty chunks. A chunk is written if it is modified, or if it still has
entities and was last saved 600 ticks ago. After a quiet stretch the next
batch can be empty (no hitch) or a full 24-chunk write of machine-dense
NBT (a stall). If more than 24 chunks are dirty, `saveChunks(false)`
returns incomplete and the next 40-tick autosave continues the drain:
one boom, or two or three cadenced booms, then quiet again. The F3 tick
slice stays low between batches and then jumps. That is why the hitch
felt random. Jumping and spinning beside the smoke towers makes the
next batch fatter: more chunks stay loaded and more enter the frustum,
so the same 40-tick save can dump a full 24-chunk write or overflow
into a second and third boom.

A late slice of the M18 live log already shows that pattern: after the
startup compile, the worst frames carry `worldSaveMs` of tens of
milliseconds and almost no compile. The skipped twin cancels those save
frames. One large non-save late frame can still appear and remains a
separate question.

The next capture must wait until chunk work has drained, keep native
40-tick saves enabled, and classify only later periodic frames. The first
fix to try is an opt-in cap below vanilla's 24-chunk non-forced batch so
one autosave cannot dump a whole dirty set in a single tick.

Promotion of a spike fix still requires a real-map capture whose worst
post-load frame is classified on this same timeline.
