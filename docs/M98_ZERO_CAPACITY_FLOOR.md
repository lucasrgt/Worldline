# M98 configured-zero protected cache floor

Status: GO in Worldline v1.86.0.

M98 runs the exact four-page M74/M78 scene with
`aero.becell.maxCachedPages=0`. Runtime validation proves the literal property
before capture. TTL remains 100000, rebuild budget eight, vanilla FPS unlimited,
and Aero pacing disabled.

Both fresh replicas retained one cached page in every complete record despite
the configured zero: 4133 and 3991 samples. All records had four page calls,
four rebuilds, zero direct fallback, and cumulative capacity-eviction delta
four. Renderer/enqueue calls stayed sixteen, flush calls stayed two, and no
flush span was zero.

This is the pinned protected-key floor. `enforceMaxCachedPages` excludes the
newly compiled key from victim selection; when it is the only entry, no victim
exists and the method returns with cache size one. Each subsequent compile
evicts the prior entry and preserves the new protected one. Configured zero
therefore does not disable paging or produce an empty cache in this path.

Nonclaims: paging disabled, other negative/positive settings, alternative
replacement policy, TTL expiry, generic content, uninstrumented/additive cost,
causality, regression/improvement, inference, pixels, cross-machine
generality, combat, or historical lag reproduction.
