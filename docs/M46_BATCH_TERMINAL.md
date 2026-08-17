# M46 Exact Batch Terminal Event

M46 adds `CorrelatedMovementRouteBatchExecution` and
`MovementRouteBatchTerminalKind`. The richer entrypoint returns the immutable
M43 result together with its exact last M44 batch event and one boundary:
`EVENT`, `AFTER_ROUTE`, or `EXHAUSTED`.

The terminal event's route index must identify the final execution and its
correlated event must be the identical terminal event retained by that
execution. The live oracle proves all three terminal kinds sequentially while
preserving the M45 unsent-alternative behavior, cache, and player persistence.

M46 adds no replay, rollback, async delivery, parallelism, registry, retry,
pathfinding, complete physics simulation, or adapter behavior.
