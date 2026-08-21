# M113 qualification cycle

`CausalLightingCycle` verifies the official server artifact, compiles the
protocol-14 adapter and M113 smoke, and runs two fresh server workspaces. Each
workspace uses two sequential client sessions: one observes and performs the
placement, while the second forces a new full-chunk light-plane observation.

The runner requires both target descriptions, ordered diff hashes, traces and
semantic signatures to match. It then checks the frozen evidence and writes a
derived local evidence file. Diagnostic or pending-signature execution cannot
qualify the milestone.

Canonical evidence uses two unmodified official servers and four client
sessions. The exact block-light delta is `68:68:0:15` for
`changed:increased:decreased:maxDelta`; the sky-light delta is `0:0:0:0`.
The frozen semantic SHA-256 is
`c54effdf42a0dcf7c37c7417e2a35d0abfdc85297b2b47398af1d4d86632c822`.
