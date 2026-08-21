# M75 qualification cycle

`AeroDensityLadderCycle` verifies the pinned Aero origin/revision and the reused
M74 server-safe closure, builds Aero in a disposable worktree, and runs eight
fresh arms in two mirrored ladders. The M74 sources and release remain unchanged;
an M75-only client Mixin redirects the exact Aero queue call.

Each arm proves real login/play, tracked plan readiness, all sixteen explicit
content messages and renderer identities, the exact level marker, complete
binary census, normal client disconnect, clean server save/stop, successful
Gradle exits, and clean worktree removal. The server always proves a sixteen-cell
present scene and never loads Aero or receives the level property.

The parser requires, record by record, positive interval time, visible chunks,
sixteen dispatches, state `16/16`, mask `0xffff`, and at-rest render/list counters
equal to the level. It also binds schema, nonce, plan, window, aggregate duration,
file length, and trailing EOF.

One retry is permitted only for the known pre-census readiness timeout shape:
trigger observed, no census start, and process timeout. The retry receives fresh
JVMs/workspace; no measured or semantic failure is retried. Partial ranges need
an explicit diagnostic flag and cannot emit qualification evidence.

The frozen semantic trace reproduces SHA-256
`92c9e4e28b17dd1df6750e5aff15022619211a1e981ffb9c3ccea461a3d9da05`.
