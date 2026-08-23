<!-- worldline-map-schema=1 -->
<!-- boundary=movement-route -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=23e11f826866e54447461ec94740a5e77d76abad7761fabcdf08d0ae5108e521 -->

# M46 Exact Batch Terminal Event

Two fresh sessions execute three safe batches and qualify all terminal kinds:
event-controller stop, after-route controller stop, and full exhaustion. Each
result must retain the exact last batch event and the identical correlated
route event exposed by its final execution.

The event-stop case retains one resolved outcome while leaving its later
alternative absent. M46 adds no event replay, rollback, asynchronous delivery,
parallelism, registry, retry, or adapter behavior.

Frozen expected signature SHA-256: `23e11f826866e54447461ec94740a5e77d76abad7761fabcdf08d0ae5108e521`
