# M35 Bounded Movement Outcome

M35 introduces `ResolvedMovementMultiplayerSession`, `MovementOutcome`, and
`MovementDisposition`. One `moveAndObserve` call sends a relative movement and
pumps a bounded number of vanilla heartbeat ticks. An actual inbound Packet13
produces `CORRECTED`; otherwise the attempted and resulting poses remain equal
and the result is deliberately named `UNCHALLENGED`.

The official smoke first requests the M25-qualified `+0.125 X` displacement.
It then selects a nearby solid block from the decoded cache and attempts to
enter it. Each of two fresh servers must correct that second move exactly to
the small-move pose, preserve the original cached chunk, and persist the same
small-move coordinates after clean disconnect and save. Persistence, rather
than silence alone, qualifies the unchallenged move as accepted evidence.

## Non-claims

M35 does not model collision shapes, gravity, continuous physics, arbitrary
paths, correction reasons, asynchronous events, or server tick control. An
`UNCHALLENGED` result outside the qualified smoke is not proof of acceptance.
