# M41 Immutable Route Termination

M41 adds `MovementRouteExecution` as an immutable summary around a controlled
explicit-fallback route. It exposes the `MovementRouteResult`, the exact final
`MovementRouteEvent`, and a `MovementRouteTermination` of `EXHAUSTED` or
`CONTROLLER_STOP`.

The terminal event must retain the identical final outcome object and have the
last global outcome index. The live oracle proves both reasons in one session:
a corrected-primary/fallback sequence stopped by its controller, followed by a
fully exhausted one-primary route. The later alternative of the stopped route
remains absent, cache stays coherent, and official player NBT persists the
exhausted route's final pose.

Existing result, observer, and controller entrypoints remain unchanged. M41
adds no goal inference, retry, asynchronous callback, event queue, server tick
control, pathfinding, complete physics simulation, or adapter behavior.
