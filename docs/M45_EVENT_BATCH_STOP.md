# M45 Event-Boundary Batch Stop

M45 adds `CorrelatedMovementRouteBatchEventController` and
`moveCorrelatedRouteBatchUntilEvent`. The controller receives the M44 indexed
event synchronously after movement resolves and before normal route control.

A returned `STOP` terminates the current route with its resolved event, marks
the batch `CONTROLLER_STOP`, and prevents every later alternative and plan from
being sent. The live oracle stops at `0/0:0:PRIMARY`, proving exactly one outcome
plus coherent cache and persisted official player state.

M45 adds no rollback, asynchronous delivery, parallelism, registry, automatic
retry, pathfinding, complete physics simulation, or adapter behavior.
