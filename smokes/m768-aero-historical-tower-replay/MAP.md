<!-- worldline-map-schema=1 -->
<!-- boundary=aero-frame-census -->
<!-- nonclaims=portable-absolute-timing,universal-causality,optimization-effect,promotion -->
<!-- frozen-trace=1c039d430fe87a0a8db8c728f462528c39a9344d08306c2388be210ecedd069c -->

# M768 Aero historical tower hitch replay behavior map

The boundary begins after a restored client reaches twenty consecutive frames
with zero terrain-compile backlog. It admits the exact central 576-machine
MEGA tower, three fixed route phases, and either native non-forced saves or the
single explicit save-suppression control.

The complete artifact contains every retained frame. Its shared sequence and
monotonic timestamp bind CPU stages, save and chunk activity, Aero logical
work, present time, allocation/heap/GC, and render-cache counters. A body digest
inside the file and a whole-file digest in evidence make truncation, mutation,
or selective-row capture fail closed.

The restored Beta save preserves the terrain but does not deserialize the
test-only synthetic MEGA block entities. Before the drain boundary, each Aero
arm therefore rehydrates the exact pinned 16 × 4 × 3 × 3 coordinate/family
matrix; the no-Aero control deliberately leaves those positions empty.

Trace: `v1|scene=mega-solid-16x4x3x3-576|sets=2|order=forward+reverse|fresh-process=true|restored-world=true|synthetic-rehydrate=exact-matrix|retained-min=600s|fps=unlimited|pacing=off|saves=native-control|phases=stationary+look-jump-spin+stationary|census=complete-frame-sha256|attribution=save+gc+chunk+aero+display+mixed+unknown|cleanup=normal`.

SHA-256: `1c039d430fe87a0a8db8c728f462528c39a9344d08306c2388be210ecedd069c`.

Signal: `sets=2,arms=8,scene=solid-16-floor-576-machine-tower,census=complete-frame-sha256,window-minimum=600-seconds`.
