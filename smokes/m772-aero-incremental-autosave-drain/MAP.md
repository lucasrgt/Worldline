<!-- worldline-map-schema=1 -->
<!-- boundary=aero-incremental-autosave-drain -->
<!-- nonclaims=no-default-enable,no-cross-machine-absolute-fps,no-save-format-change -->
<!-- frozen-trace=33727d7790d03e66de2fba1ab6e504570e184aface5a04c81927d8f03a4e18ef -->

# M772 Aero incremental autosave drain behavior map

M772 binds Aero's opt-in `aero.world.incremental-autosave` candidate to the
neutral `worldline.profiling.HitchRateGate`. Eight measured StationAPI clients
restore the same multi-chunk ULTRA machine field in four counterbalanced pairs
ordered AB, BA, BA, AB. Reference arms retain vanilla's non-forced chunk limit;
candidate arms limit each non-forced pass to one chunk and resume traversal
after the last chunk visited by the previous bounded pass.

Every measured world begins clean, receives one sentinel mutation in each of
twelve chunks, and advances through Minecraft's native 40-tick autosave cadence.
The active machines can dirty hundreds of loaded chunks repeatedly, so the cycle
requires both arms to make progress across at least twelve distinct chunks before
tick 540. It then mutates all twelve sentinels again, invokes a forced save, and
requires every eligible dirty chunk to reach zero. A fresh verifier client
reloads the same save and observes all twelve final sentinels.

The cycle records complete frame durations and save-call timing. Candidate arms
must write at most one chunk per non-forced call, every arm must write at least
twelve chunks across at least twelve distinct chunk identities, and the
candidate maximum save duration must be smaller in at
least three of four pairs. The existing 50 ms paired hitch-rate gate must report
no regression beyond 500 parts per million.

This qualification proves bounded non-forced work, complete forced drainage,
reload persistence, and paired hitch-rate safety in the controlled scene. It
does not enable the candidate by default, claim portable absolute FPS, or alter
Minecraft's save format.

Expected signal:
`scene=ultra-12-chunk,pairs=4,arms=8,autosave=40-tick,budget=1,progress=12-unique,forced=all-to-0,reload=12,hitch=no-regression,save-max=majority-smaller`.

Frozen semantic SHA-256:
`33727d7790d03e66de2fba1ab6e504570e184aface5a04c81927d8f03a4e18ef`.
