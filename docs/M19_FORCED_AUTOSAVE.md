# M19 Forced Autosave

M19 turns the M18 save-path attribution into a forced hitch and an opt-in
cap. Native `World.autosavePeriod` stays at 40 ticks. The capture mixin
marks 60 loaded chunks modified at tick 20, then the look-jump-spin path
stays beside the smoke tower.

## What is compared

Three restored copies of the same dense save run the same path:

- `live` keeps vanilla's 24-chunk non-forced batch;
- `budgeted` sets `-Dworldline.saveBudget.chunks=1` on mapped
  `ChunkCache.save`;
- `skipped` keeps Aero's test mixin cancelling non-forced saves.

The budget mixin lives in the Worldline capture overlay. A missing or zero
property, and every forced save, keep the vanilla 24-chunk batch. The
pinned Aero checkout is not edited.

## What must hold

The live window must show at least two save frames and a worst save of at
least 20 ms. The skipped twin must record none. The budgeted twin must
still save, and its worst save must be smaller than the live worst save.
Those magnitudes stay observational. On this machine the live worst save
was 68.7 ms across five save frames; the one-chunk budget kept five save
frames and cut the worst save to 13.5 ms. The frozen report is qualitative:

```text
scene=LOOK_JUMP_SPIN_TOWER
budget=ONE_CHUNK_NON_FORCED_OPT_IN
live=CADENCED_BATCH
budgeted=SMALLER_MAX_SAVE
skipped=SAVE_CANCELLED
```

## What remains a non-claim

A synthetic 60-chunk dirty set is not the user's machine-dense map. The
cap is not enabled by default, because other mods can need a faster flush
after a crash. One remaining fat chunk after the one-chunk cap is a later
NBT question, not a frustum rewrite. The adaptive scheduler stays
lab-only NO-GO. The M16 framebuffer threshold is unchanged.
