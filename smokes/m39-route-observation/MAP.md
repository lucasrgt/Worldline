<!-- worldline-map-schema=1 -->
<!-- boundary=movement-route -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=df2973b510807bc1ebce5b49ba2921e14137bd2970fba351d61df46f44165222 -->

# M39 Synchronous Route Observation

Two fresh sessions execute the M38 safe-primary/corrected-primary/fallback
sequence with a synchronous observer. It must receive the exact indexed events
`0:0:PRIMARY`, `1:1:PRIMARY`, `1:2:FALLBACK` on the caller thread, immediately
after each bounded movement resolves.

Each event must contain the identical outcome object later exposed by the
immutable route result. The observer adds no game callback or thread. Cache
remains coherent and official player NBT persists the final fallback pose.

Frozen expected signature SHA-256: `df2973b510807bc1ebce5b49ba2921e14137bd2970fba351d61df46f44165222`
