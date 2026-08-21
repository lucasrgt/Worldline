# M97 page-capacity-one thrash

Status: GO in Worldline v1.85.0.

M97 runs the exact M74/M78 four-page, sixteen-cell scene with one cached page.
TTL remains 100000 frames, rebuild budget eight, vanilla FPS unlimited, and
Aero pacing disabled. This keeps expiry and rebuild-budget fallback out of the
retained window.

Both fresh replicas rebuilt all four pages in every complete record: 5067 and
4581 samples, respectively. Cache count stayed one, page calls stayed four,
direct fallback stayed zero, and cumulative capacity evictions advanced exactly
four per record. `rebuild3` remained zero, distinguishing this boundary from
M96's capacity-two tie behavior. Renderer/enqueue calls were sixteen, flush
calls were two, and no flush span was zero.

For this exact sorted fixture, one cache entry cannot preserve a page through
the full four-key flush: each newly protected compile displaces the previous
entry. Timing values remain descriptive and are not used as release thresholds.

Nonclaims: other capacities/topologies, TTL expiry, rebuild-budget fallback,
generic content, uninstrumented/additive cost, causality, regression or
improvement, inference, pixels, cross-machine generality, combat, or historical
lag reproduction.
