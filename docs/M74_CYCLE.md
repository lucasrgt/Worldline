# M74 qualification cycle

`AeroFrameCensusCycle` verifies the pinned Aero origin and revision, checks the
server-safe source closure, builds Aero in a disposable detached worktree, and
copies the derived JAR to ignored evidence storage. The pinned checkout remains
clean before, between, and after arms.

The canonical run executes four fresh server/client arms in `present/absent`,
`absent/present` order. Every arm uses a new StationAPI server, graphical Aero
client, world, game directory, and pair of detached worktrees. Within each pair,
the runner requires the same plan, raw block ID, nonce, fixed camera, seed,
content definition, heap, window, and instrumentation.

Each client proves login/play, warm-up, tracked plan readiness, exact fixture
reconciliation, census start, a strictly parsed binary artifact, normal
disconnect, and successful Gradle exit. Each server proves activation before
tracking readiness before scene construction, then clean disconnect, save, and
stop without loading Aero. Partial arm ranges require an explicit diagnostic
flag and cannot emit qualification evidence or milestone wording.

The binary parser validates the v1 header and 28-byte records before trusting
counts, rejects overflow/trailing bytes, uses exact arithmetic for elapsed time,
and enforces zero structural work in absent arms and nonzero real Aero/content
work in present arms. The frozen semantic trace reproduces SHA-256
`2cc4533688aa06ba1d69309639c36e16688b09eb4deeeb27d044277550d2d1a7`.
