# M17 Completion Cycle

Status: **GO for matrix qualification; NO-GO for upstream promotion**.

| Requirement | Result |
| --- | --- |
| Preserve the pinned Aero checkout and canonical saves | PASS |
| Run stationary-empty, stationary-dense, and moving-dense | PASS |
| Compare vanilla, old governor, and adaptive | PASS |
| Preserve one explicit adaptive completion per frame | PASS |
| Drain visible and background work without starvation | PASS |
| Retain stationary normalized readiness parity or better | PASS |
| Retain moving-window readiness parity | FAIL; eventual drain only |
| Enforce a hard 12 ms deadline inside one rebuild | FAIL; non-preemptive |
| Retain strict M16 full-frame pixel parity | FAIL in all three checkpoints |
| Package the evaluation profile default-off | PASS |
| Promote the candidate into Aero | NO-GO |

Frozen M17 evidence SHA-256:
`27add12df594ab97e330b33fc0118918fcbabbf3189f171089967fdafe5955a0`.

The milestone is complete because the executable matrix and its negative
decision are frozen. It does not convert failed promotion criteria into passes.
