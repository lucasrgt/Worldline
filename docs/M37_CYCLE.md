# M37 Completion Cycle

Status: **GO for stop-on-correction route policy**.

| Requirement | Result |
| --- | --- |
| Preserve M36 continue behavior as default | PASS |
| Expose explicit stop-on-correction policy | PASS |
| Record the corrected outcome before stopping | PASS |
| Execute exactly two of three supplied steps | PASS |
| Retry the corrected step zero times | PASS |
| Preserve cache and persist the stopped pose | PASS |
| Discover alternate paths or model physics | NOT RUN |

The final implementation passed three consecutive cycles, covering six fresh
official-server scenarios.

Frozen M37 semantic SHA-256:
`4a9a43b61c171fd05ab6156b07c963b7c1ebcdedc6ab7ea42d7a40db04cdf649`.
