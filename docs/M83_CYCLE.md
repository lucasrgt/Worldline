# M83 qualification cycle

`PageTopologyContrastCycle` builds pinned Aero in a disposable worktree,
verifies the complete Aero-free common/server closure, and runs fresh
same-page and cross-page graphical-client/modded-server arms.

After retained record 300, each client sends one typed request with plan,
nonce, and topology code. Both requests remove exactly two members. The server
preflights every target and derived nonce, applies both removals, and returns
an exact ACK. A fixed 44-byte sidecar binds topology, expected rebuild count,
request/event indices, nonce, and plan after the complete census seals.

The M74 parser requires live identity membership `16 -> 14`, state `0x1010`,
mask `0xffff`, positive visibility, and zero renderer/list counters. The M78
parser requires two pages/calls, two flush calls, zero fallback, and exactly
one versus two event rebuilds with zero rebuilds elsewhere.

Artifact schemas, lengths, EOF, SHA-256 markers, record count, elapsed window,
plan, nonce, camera, server/client lifecycle, and clean worktrees are checked.
Diagnostic mode runs one topology but cannot qualify or emit release evidence.

The frozen semantic trace reproduces SHA-256
`2418e988f23571a72a07c2521eb9ee7cb9ebc8b436957a74d7cf226fe4878f10`.
