# M75 Aero exposure ladder

Status: GO in Worldline v1.63.0.

M75 uses the complete M74 census to acquire two mirrored fresh ladders at Aero
call levels `0, 1, 4, 16`. The server-authored scene is deliberately constant:
all arms contain the same sixteen synchronized block entities, fixed camera,
plan, dispatch load, and census machinery. Only a test-only redirect controls
how many nested renderer identities enter the real Aero at-rest boundary.

The client level is absent from the server JVM. The runner requires every binary
record to contain sixteen renderer dispatches, complete synchronized state and
identity mask, and exactly the configured number of Aero renders/list calls.
This makes treatment integrity stronger than comparing only final totals.

The first ladder runs `0/1/4/16`; the second runs `16/4/1/0`. Every arm has fresh
server/client JVMs, worktrees, worlds, and game directories. Within a ladder,
the nonce and concrete plan are identical. All levels retain the M74 warm-up and
at least 720 complete renderer intervals plus twelve seconds.

M75 reports whole-census medians, p95s, p99s, maxima, and level-minus-zero
differences. Those numbers and their ordering are dynamic observations, never
promotion gates. Two ladders do not establish monotonicity, dose response,
causality, significance, regression, isolated Aero cost, cross-machine
generality, or reproduction of the historical lag mechanism.
