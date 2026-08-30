# M772-AERO-INCREMENTAL-AUTOSAVE-DRAIN Aero incremental autosave drain

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

Four counterbalanced pairs qualify Aero's opt-in fair one-chunk non-forced autosave budget in a restored multi-chunk ULTRA scene while preserving forced drainage, reload persistence, and neutral hitch-rate safety.

## Qualification cycle

M772 runs a native 40-tick autosave cadence across eight measured clients, requires progress across at least twelve distinct chunks before tick 540, forces every eligible dirty chunk to zero after a second twelve-chunk mutation, verifies all sentinels in eight fresh clients, and requires a smaller maximum save in at least three pairs without a hitch-rate regression.

Expected signal: `scene=ultra-12-chunk,pairs=4,arms=8,autosave=40-tick,budget=1,progress=12-unique,forced=all-to-0,reload=12,hitch=no-regression,save-max=majority-smaller`.

Frozen semantic SHA-256: `33727d7790d03e66de2fba1ab6e504570e184aface5a04c81927d8f03a4e18ef`.
