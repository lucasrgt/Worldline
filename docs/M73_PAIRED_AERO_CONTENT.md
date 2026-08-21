# M73 paired Aero content boundary

Status: GO in Worldline v1.61.0.

M73 composes M71's paired acquisition discipline with M72's real synchronized
Aero content. Both arms load the same universal content mod and pinned Aero
client. After at least 300 renderer frames and five seconds, the client sends the
same identifier-scoped activation message carrying the pair nonce.

The server then sends the concrete plan and waits for a client acknowledgement
that the fixed camera has arrived, every target chunk is loaded, and all sixteen
target cells are empty. This tracked-plan handshake replaces timing-based
placement readiness.

The first arm establishes a fixed-seed world-spawn-anchored sixteen-cell plan and the runner supplies
that exact plan to the paired arm. The absent arm validates it but places,
synchronizes, and renders zero cells. The present arm places a compact 4x4 wall, receives
sixteen explicit server-authored coordinate-and-nonce messages, reconciles sixteen exact block
entities, and observes sixteen unique real Aero renderer returns. Both arms
traverse four server-tick treatment phases; present places four cells per phase. Extra unique,
conflicting, or mistyped cells fail closed; an exact duplicate is idempotent.

Each arm then completes at least 720 renderer frames and twelve seconds. The
runner selects at least 45 post-trigger Aero FrameSpike/GC/Pulse rows, excludes
WorldFlush, parses nonnegative typed fields, and requires those stdout rows in
the normally flushed file. File sync remains disabled.

Two fresh pairs use opposite orders, `present/absent` then `absent/present`.
Within a pair the seed, mod, name, heap, logger, and pair nonce match. Every arm
uses fresh server/client JVMs, worktrees, worlds, and game directories.

M73 reports descriptive selected-row medians, p95s, maxima, renderer activity,
and present-minus-absent deltas. Mixed signs are valid observations. The release
does not claim causality, statistical significance, a regression or improvement,
complete frame census, pixel correctness, density response, Aero cell batching,
cross-machine generality, or reproduction of the historical lag mechanism.
