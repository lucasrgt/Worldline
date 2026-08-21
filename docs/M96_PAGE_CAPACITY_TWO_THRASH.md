# M96 page-capacity-two bounded thrash

Status: GO in Worldline v1.84.0.

M96 runs M78's exact four-page, sixteen-cell scene with a two-entry Aero page
cache. TTL stays at 100000 frames, rebuild budget at eight, vanilla frame limit
at zero, and Aero pacing disabled. The boundary therefore isolates capacity
replacement from expiry and rebuild-budget fallback.

The replacement path is not identical across fresh JVMs. One canonical
replica retained 4980 records with three rebuilds per record; the other retained
4552 records with four. In every record, however, cache size was two, page
calls were four, direct fallback was zero, and the cumulative capacity-eviction
delta exactly equaled that record's rebuild count. Both had sixteen renderer
calls/enqueues, two flush calls, and no zero flush span.

This qualifies bounded tie behavior, not a universal formula. Pages sharing
the same `lastUsedFrame` are selected through pinned `HashMap` iteration, so a
fresh JVM may preserve one prior key or rebuild all four. The semantic contract
accepts only rebuild counts three/four with exact eviction coupling; it does
not prescribe which mode a replica chooses.

Nonclaims: exhaustive JVM/hash outcomes, other capacities, replacement-policy
generality, TTL expiry, generic content, uninstrumented/additive cost,
causality, regression/improvement, inference, pixels, cross-machine
generality, combat, or historical lag reproduction.
