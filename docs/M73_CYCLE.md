# M73 qualification cycle

`AeroPairedContentCycle` verifies the pinned Aero origin/revision and common
server-safe source closure, builds Aero in a detached disposable worktree, and
copies the derived JAR into ignored evidence storage. The pinned checkout remains
unchanged.

The canonical cycle runs four fresh arms in `P/A, A/P` order. Each server must
load StationAPI and Worldline content without Aero; each graphical client must
load the same content plus Aero 3.0.0. Every arm proves exact login/play markers,
one activation, one fixed-seed world-spawn-anchored sixteen-cell plan reused exactly by its paired arm, its mode-specific placement/sync/render
counts, an exact tracked-plan readiness acknowledgement before placement, the
fixed warm-up/window, selected rows, normal disconnect, save/stop,
successful Gradle exits, and clean worktrees.

Windows Loom can briefly retain a generated intermediary JAR or race the shared
StationAPI sources remap after a process exits. The runner permits one
three-second retry only for those two exact diagnostics; every other error remains terminal. Partial arm
ranges require an explicit diagnostic property and cannot emit qualification
evidence or milestone wording.

The two qualification pairs produced descriptive selected-row deltas with mixed
signs. Those dynamic values remain evidence, not release gates. The frozen trace
reproduced SHA-256
`41422dda87ca7a8ed192e8c23c9946c55518f87e123cf69d6b1662d689b3b500`.
