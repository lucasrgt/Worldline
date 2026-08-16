# M13 Aero Persistence and Chunk Differential

M13 separates three questions that M12 left coupled: which BlockEntities
persist, whether Aero content is required for the compile spike, and whether
Aero's optional chunk-compile governor is a safe mitigation.

## Persistence

In the qualification run the fresh scene reported 1,176 global BlockEntities
but only 576 coordinates containing a real `BlockWithEntity`. Vanilla rejects
the excess placements with `Attempted to place a tile entity where there was no
entity tile`, yet they remain temporarily visible through the world's global
list. Reload reported 597 global BlockEntities and the same 576 real entity
blocks. The real fixture blocks persist; roughly half of the fresh global list
is phantom state and disappears on reload.

## Dense versus empty

Both modes use seed `17320110707`, the same 3 by 3 chunks, camera, 60-tick
warmup, and 120 measured ticks. The empty mode disables the Aero test fixture
while retaining Aero, StationAPI, Minecraft, and the instrumented chunk path.
Exploratory repetitions produced chunk-compilation spikes in both modes. In one
run their final 200-frame windows contained 628 and 747 compile calls, with
compile p95 of 15.0 and 11.5 ms. Another repetition peaked just below the 25 ms
frame threshold while retaining 10+ ms compiles in both modes. The gate freezes
substantial compile pressure rather than a scheduler-sensitive threshold.
Aero content is not necessary for that path, and a repeatable dense-scene
amplification was not established.

## Budget experiment

The main render caller passes `forced=false`; M14 corrected the earlier forced-call
description. The default governor still does not activate while the scene
threshold is positive because its work counters become positive after the
compile decision. With a zero threshold the mechanism activates and reduced sampled spikes, but it
returned `false` 31,742,420 times for only 324 accepted compiles in the final
200-frame window, still produced three compile spikes, and reached a 61.8 ms
maximum compile stage. The caller immediately retries deferred work, creating
a busy retry storm. M13 rejects this governor configuration as a mitigation.

M14 subsequently measured the caller and dirty queue, attributed most stable
pressure to an initial backlog, and prototyped bounded accepted work without
returning a retryable failure in the hot loop.
