# M20 Completion Cycle

Status: **GO for official server identity and dedicated lifecycle control**.

| Requirement | Result |
| --- | --- |
| Keep the official server JAR outside Git | PASS |
| Freeze byte length, SHA-1, and SHA-256 | PASS |
| Acquire over HTTPS and fail closed on drift | PASS |
| Boot two fresh localhost-only dedicated servers | PASS |
| Reach native `Done`, issue `stop`, save, and exit cleanly | PASS |
| Decompile or modify the server | NOT RUN |
| Connect an official client | NOT RUN |
| Claim deterministic multiplayer | NOT RUN |

Frozen M20 lifecycle SHA-256:
`7d1edb19b978300465878cfade247ec0db7db37b9a5fbcfd9a595566bfb06b60`.

M20 is the legal and executable foundation for server-side and multiplayer
milestones. It promotes no server automation API yet.
