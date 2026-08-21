# M103 qualification cycle

`PagesDisabledImmediateDirectCycle` runs two fresh graphical-client/modded-
server replicas with pinned Aero, shared plan/nonce, strict camera, and an
Aero-free common/server closure.

The client rejects pages other than false, cache maximum other than one,
rebuild sentinel other than negative one, or TTL other than100000. The 60-byte
sidecar record adds a smoke-owned immediate-direct count to the prior timing
and public page counters. Every aligned record must report queue entries16,
immediateDirect16, flush2, public cell state all-zero, and M74 counters16/16.
The runner also verifies schema, identity, length, EOF, hashes, lifecycle,
provenance, clean shutdown, and worktree cleanup. Diagnostic mode cannot
qualify or emit evidence.

The canonical replicas retained 4021 and 3673 complete records; every one
followed the exact pages-disabled immediate-direct path. All descriptive
timing values remain dynamic and outside the release signature.

The frozen semantic SHA-256 is
`7ebb83eada0eccda5dbb38d2610d92b60abe893d2483212710eb463e0aa285c6`.
