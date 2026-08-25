# FRONT2-08 fishing and flint random-boundary decision

Worldline registers the remaining fishing and gravel randomness as an explicit
non-claim. This closes a silent coverage gap without turning one lucky random
draw or a decompiled probability into a behavioral guarantee.

## Existing qualified boundary

M180 proves that using fishing rod `346` creates the official hook object.
M360 proves an event-triggered catch of raw fish `349`. It deliberately excludes
the successful attempt number, exact bite timing, bobber timing, alternate loot,
and any relationship between pool depth and loot.

M218, M274, M342, and M598 prove gravel placement and gravity outcomes. They do
not claim that a particular gravel break drops flint `318`, an exact flint drop
rate, or a statistical distribution.

## Decision

FRONT2-08 selects an **explicit non-claim with a bounded-RNG reopening rule** for
all three uncovered surfaces:

- fishing bite timing;
- fishing loot depth or loot distribution;
- gravel-to-flint drop rate.

No current release claim may infer those outcomes from M360 or M598. A future
milestone may reopen one surface only with an official Beta 1.7.3 oracle and
two fresh replicas, plus a declared finite attempt bound and a reusable public TestKit
contract such as `BoundedAttempts.until`. The frozen evidence must describe the
post-event state and exclude the successful attempt number. A probability or
rate claim additionally requires a reviewed deterministic draw matrix; observing
one success inside the bound is not rate evidence.

The machine-readable decision is
`quality/front2-random-boundary.properties`. The canonical Gate verifies the
decision document, the existing qualified boundaries, the public bounded-attempt
primitive, and the absence of a smoke that silently claims these identities.
