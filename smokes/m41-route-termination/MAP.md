<!-- worldline-map-schema=1 -->
<!-- boundary=movement-route -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=f3134a8e626058fc196b5ad3787199c6e0cd7f71a25a8a5db228289b886cdf7a -->

# M41 Immutable Route Termination

Each fresh session executes one controller-stopped explicit-fallback route and
one fully exhausted route. The first must terminate at `1:2:FALLBACK` with
`CONTROLLER_STOP`; the second must terminate at `0:0:PRIMARY` with `EXHAUSTED`.

Each terminal event must be the exact last observed event and retain the exact
last outcome object in its immutable result. The summary does not infer goals,
retry movement, schedule work, or alter adapter behavior. Cache remains
coherent and official player NBT persists the final exhausted-route pose.

Frozen expected signature SHA-256: `f3134a8e626058fc196b5ad3787199c6e0cd7f71a25a8a5db228289b886cdf7a`
