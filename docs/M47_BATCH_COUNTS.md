# M47 Immutable Batch Counts

M47 adds `MovementRouteBatchCounts` to every
`CorrelatedMovementRouteBatchResult`. It records completed routes, resolved
outcomes, and authoritative corrections within the existing limits of 16
routes and 64 outcomes per route.

Counts are computed once from immutable route results during batch-result
construction. The original executions, outcomes, correlated events, and exact
terminal event remain identity-bound and are neither replayed nor flattened.

The live oracle exhausts two safe plans containing three total alternatives and
proves `2 / 3 / 0`, coherent cache, and persisted official player state.
