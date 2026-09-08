# FRONT2-08 fishing and flint random-boundary decision

Worldline now makes a bounded official claim for the finite native random-outcome
matrix in `b173-native-random-outcomes-cycle`. Four fresh JVMs compare mapped and
official Beta 1.7.3 oracle results, with two fresh replicas on each side.

## Existing qualified boundary

M180 proves that using fishing rod `346` creates the official hook object.
M360 proves an event-triggered catch of raw fish `349`. It deliberately excludes
the successful attempt number, exact bite timing, bobber timing, alternate loot,
and any relationship between pool depth and loot.

M218, M274, M342, and M598 prove gravel placement and gravity outcomes. They do
not claim that a particular gravel break drops flint `318`, an exact flint drop
rate, or a statistical distribution.

## Decision

The reviewed deterministic draw matrix reopens three related surfaces:

- fishing bite timing;
- fishing loot depth or loot distribution;
- gravel-to-flint drop rate.

`RandomOutcomeFixture.verify` checks all ten gravel residues (one flint), all
500 dry and 300 exposed-rain bite residues at depths one, three and five, all
30 catch-window durations, and first-bite schedules at ticks one, five and
seventeen. The complete ordered evidence contains 3,228 observations. Conditional
uniform integer draws yield flint probability 1/10 and eligible-tick bite
probabilities 1/500 or 1/300. Native catches produce one raw fish at each depth.

The new proof does not retroactively widen M360 or M598. Event-only consumers of
`BoundedAttempts.until` must still exclude the successful attempt number. The new
matrix claims only its explicitly controlled schedules; seed quality, an
unconditioned waiting-time distribution, moving-hook trajectories, arbitrary
water geometry and later-version loot tables remain outside the boundary.

The machine-readable decision is
`quality/front2-random-boundary.properties`. The canonical Gate verifies the
decision document, the existing qualified boundaries, the public bounded-attempt
primitive, the new package's public binding and frozen four-process evidence,
and the absence of unrelated smokes silently claiming broader identities.
