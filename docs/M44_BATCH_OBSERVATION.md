# M44 Synchronous Batch Observation

M44 adds `CorrelatedMovementRouteBatchEvent` and
`CorrelatedMovementRouteBatchObserver`. The four-argument batch entrypoint emits
one event immediately after each resolved movement and before its existing route
controller decision.

The batch event adds a bounded route index from 0 through 15 and retains the
exact correlated route event. The live oracle exhausts two one-step routes and
observes `0/0:0:PRIMARY` then `1/0:0:PRIMARY` on the caller thread, preserving
both correlation identities, cache coherence, and final player persistence.

M44 adds no control decision, asynchronous delivery, parallelism, registry,
retry, scheduling, pathfinding, or adapter behavior.
