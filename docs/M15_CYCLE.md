# M15 Completion Cycle

Status: **GO for explicit contract and readiness evidence; fixed batch rejected**.

| Requirement | Result |
| --- | --- |
| Preserve the pinned Aero checkout | PASS |
| Keep only per-file source limits | PASS |
| Replace Boolean ambiguity with accepted/deferred states | PASS |
| Execute complete, accepted/deferred, and stalled/deferred outcomes | PASS |
| Resume deferred work on the next frame, not the current hot loop | PASS |
| Measure dirty age and visible readiness from the first world frame | PASS |
| Hash exact chunk vertex streams under both policies | PASS |
| Compare exact non-empty geometry and expose temporal divergence | PASS |
| Preserve visible readiness with a fixed batch of two | FAIL — POLICY REJECTED |
| Freeze the evidence conclusion and non-claims | PASS |

Frozen M15 evidence SHA-256:
`aa3b77e6531cd832f75f9afab1c79abf2427bf7341b34b01578d6bf0cb445a73`.

The milestone is successful because it answers the policy question with
executable evidence; it does not promote the rejected policy. The canonical
gate is `java tools/harness/Verify.java --smoke`. No local game binary, Aero
checkout change, save, runtime log, or generated geometry record is released.
