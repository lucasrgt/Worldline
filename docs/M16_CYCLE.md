# M16 Completion Cycle

Status: **GO for adaptive scheduling; post-overlay pixel parity not reproduced**.

| Requirement | Result |
| --- | --- |
| Preserve the pinned Aero checkout | PASS |
| Keep only per-file source limits | PASS |
| Retain explicit accepted/deferred next-frame semantics | PASS |
| Invoke the contract once per rendered frame | PASS |
| Select visible dirty work before background work | PASS |
| Bound accepted work and elapsed rebuild work | PASS |
| Close the initial visible-readiness gap against baseline | PASS |
| Freeze tick, camera, interpolation, HUD, and view bobbing | PASS |
| Restore one canonical save into both measured processes | PASS |
| Drain both global queues and stabilize the visible set | PASS |
| Compare every RGBA pixel against the original strict threshold | DIVERGENCE DETECTED |
| Promote the candidate into Aero or the public API | NOT CLAIMED |

Frozen M16 evidence SHA-256:
`f274b0970e16939ba56b8f8796360d54c5f7981168a1e52e9d85da95585eb26b`.

M17 corrected the startup overlay and reproduced visual divergence across a
broader matrix, so the scheduler is no longer a promotion candidate. The
canonical gate is `java tools/harness/Gate.java --smoke`. No local game
binary, Aero checkout change, save, runtime log, or framebuffer is released.
