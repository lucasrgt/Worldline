<!-- worldline-map-schema=1 -->
<!-- boundary=b173-inert-solid-stability-envelope-cycle -->
<!-- nonclaims=sponge-fluid-neighbors,snow-block-high-light-melt,native-render,arbitrary-neighbor-types,unbounded-temporal-invariance -->
<!-- frozen-trace=0000000000000000000000000000000000000000000000000000000000000000 -->

# Beta 1.7.3 inert-solid bounded stability envelope

Fifteen caller-owned rows cover cobblestone, six ore and mineral-block subjects, bricks, mossy
cobblestone, obsidian, clay, and netherrack. One maintained public TestKit family gameplay-places
each block beneath a direct stone overhead, observes it across two hundred official server ticks,
removes the overhead through normal pickaxe breaking, observes forty more ticks, and crosses a
clean save plus fresh login.

The tick window is an intentional bounded observation contract, not an asynchronous sleep. The
overhead transition is causal: the official server removes an adjacent block and emits its native
neighbor notification before the target is checked. Together the rows close thirty Functional
Census atoms inside one coherent stability mini-subsystem.

Sponge remains outside this archetype because its conformance route is singular and fluid-adjacent
contexts require their own oracle. Snow block remains outside because its high-light random tick
melts it; a dark-canopy survival sample would not establish that policy. This map does not claim
native rendering, arbitrary neighbor identities, longer time windows, or unbounded invariance.

The frozen aggregate signal and semantic signature are populated only after exact official-runtime
discovery.

Frozen signal:
`provider=b1.7.3-server-lifecycle,family=inert-solid-stability-envelope,rows=15,passed=15,claims=30,tick-window=200,neighbor=stone-overhead-remove,reload=FRESH_LOGINx15,evidence=0000000000000000000000000000000000000000000000000000000000000000,isolation=15-fresh-worlds`.
