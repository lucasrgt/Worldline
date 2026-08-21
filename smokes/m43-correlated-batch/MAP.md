# M43 Bounded Correlated Route Batch

Two fresh sessions submit two correlated route plans. The first safe route
must finish as `EXHAUSTED`; the synchronous batch controller then returns
`STOP`, so the second route is never sent.

The immutable batch result contains exactly one identity-correlated execution
and reports `CONTROLLER_STOP`. The batch is capped at 16 plans and adds no
parallelism, registry, retry, scheduling, or adapter behavior. Cache remains
coherent and official player NBT persists the first route's pose.

Frozen expected signature SHA-256: `3b09e9188cd0948cb17f11f3f203888bfd04845bf599ea20fbd004b1d1a94e44`
