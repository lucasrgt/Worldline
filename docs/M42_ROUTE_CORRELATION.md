# M42 Caller-Owned Route Correlation

M42 adds `CorrelatedMovementRouteEvent`,
`CorrelatedMovementRouteController`, and `CorrelatedMovementRouteExecution`.
`moveRouteWithFallbackCorrelated` attaches one non-null caller-owned reference
to every synchronous route event and the exact terminal summary.

Worldline preserves the reference by identity. It does not inspect, serialize,
compare by value, retain globally, or assign meaning to it. The live oracle
proves the same reference across a safe primary, controller stop, and terminal
summary while a later movement remains absent. Cache stays coherent and
official player NBT persists the accepted pose.

M42 adds no global registry, retry, asynchronous callback, event queue, server
tick control, pathfinding, complete physics simulation, or adapter behavior.
