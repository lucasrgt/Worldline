# M44 Synchronous Batch Observation

Two fresh sessions exhaust two one-step correlated routes. The batch observer
must receive route indexes `0` and `1` on the caller thread while each embedded
event retains its own `0:0:PRIMARY` indexes and correlation identity.

Observation occurs before the existing route control decision and does not
change it. The batch adds no asynchronous delivery, parallelism, registry,
retry, or adapter behavior. Cache remains coherent and official player NBT
persists the second route's pose.
