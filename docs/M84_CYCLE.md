# M84 qualification cycle

`FourPageTopologyContrastCycle` builds pinned Aero in a disposable worktree,
verifies the complete Aero-free common/server closure, and runs fresh
one-page and three-page graphical-client/modded-server arms.

After retained record 300, each client sends a typed request containing plan,
nonce, and topology code. Both requests remove exactly three members. The
server preflights every target and derived nonce, applies all removals, and
returns an exact ACK. A fixed 44-byte sidecar binds topology, expected rebuild
count, request/event indices, nonce, and plan after the complete census seals.

The M74 parser requires live identity membership `16 -> 13`, state `0x1010`,
mask `0xffff`, positive visibility, and the constant singleton counters
`render=1/list=1`. The M78 parser requires three cached pages/page calls, two
flush calls, one direct fallback, and exactly one versus three event rebuilds
with zero rebuilds elsewhere.

Artifact schemas, lengths, EOF, SHA-256 markers, record count, elapsed window,
plan, nonce, camera, server/client lifecycle, and clean worktrees are checked.
Diagnostic mode can run one topology but cannot qualify or emit release
evidence.

The frozen semantic trace reproduces SHA-256
`ab9789101de12052aa945af741a37394c4a4b06fb78fa2d3d0737120a45eb39b`.
