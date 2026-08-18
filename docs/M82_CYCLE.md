# M82 qualification cycle

`NaturalWaveLadderCycle` builds pinned Aero in a disposable worktree, verifies
the Aero-free server/common closure, and runs three fresh modded-server/real
graphical-client arms for target counts one, two, and four. All arms use seed
81081, nonce 8208201, exact plan `(10,65,31)`, camera `-90/0`, and the same
warmup/census/page configuration.

After record 300, each client sends one typed request containing plan, nonce,
and cardinality. The server validates the exact target set before applying all
removals and acknowledging. A fixed 44-byte post-seal sidecar binds target
count, expected rebuild count, request/event indices, nonce, and plan.

The M74 parser requires state `0x1010`, mask `0xffff`, positive visibility,
zero renderer/list counters, and live identity membership `16 -> 15/14/12`.
The M78 parser requires two cached pages/page calls, two flush calls, zero
fallback, and event rebuilds `1/2/2`, with no rebuild outside the transition.

Artifact schema, length, EOF, SHA-256 marker, record count, elapsed window,
plan, nonce, and server/client lifecycle are cross-checked. A diagnostic target
property can run one arm but explicitly cannot qualify or write release
evidence.

The frozen semantic trace reproduces SHA-256
`2727138a7c9b2eb9e38b7a40a9ae8518a3c3c7b0739c188d2ae152edbbb47bab`.
