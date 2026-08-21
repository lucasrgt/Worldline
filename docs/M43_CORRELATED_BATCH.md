# M43 Bounded Correlated Route Batch

M43 adds immutable `CorrelatedMovementRoutePlan` and
`CorrelatedMovementRouteBatchResult` values. `moveCorrelatedRouteBatch`
executes at most 16 plans sequentially, preserving each M42 correlated
execution and its M41 route termination.

After each completed route, a synchronous batch controller returns `CONTINUE`
or `STOP`. A stop is applied before the next plan is sent. The live oracle
proves one exhausted safe route, a batch stop, and a second absent route while
cache and official player persistence remain coherent.

M43 adds no parallelism, global registry, retry, scheduling, pathfinding,
server tick control, complete physics simulation, or adapter behavior.
