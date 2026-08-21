# M19 Forced Autosave Evidence Map

## Force

The runner restores the same dense Aero save as M18. Measured processes stay
next to the smoke tower, jump on a 10-tick bob, and spin the camera 12
degrees per tick — the lived trigger (jumping and turning beside the
machine fixtures). At tick 20 it also marks 60 loaded chunks modified so
the next native autosaves have a full dirty set. Native
`World.autosavePeriod` (40 ticks) stays enabled. Vanilla writes at most 24
dirty chunks per non-forced save, so a 60-chunk dirty set should produce
two or three cadenced `worldSaveMs` peaks.

Three measured twins share that save and path:

- `live`: vanilla 24-chunk batch;
- `budgeted`: opt-in `-Dworldline.saveBudget.chunks=1` on `ChunkCache.save`;
- `skipped`: Aero test mixin cancels non-forced saves.

No save is injected. The Aero checkout is unchanged. The budget mixin lives
in the Worldline capture overlay and stays default-off.

## Decision

This capture attributes the quiet-then-boom F3 pattern and proves that an
opt-in one-chunk non-forced cap reduces the worst save versus the live
batch. It does not declare the historical spike on a real machine-dense
map eliminated, and it does not enable the cap by default.
