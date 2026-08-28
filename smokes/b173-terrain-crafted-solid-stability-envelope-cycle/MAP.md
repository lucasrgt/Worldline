<!-- worldline-map-schema=1 -->
<!-- boundary=b173-terrain-crafted-solid-stability-envelope-cycle -->
<!-- nonclaims=occupied-mineral-stability,grass-redstone-ore-random-tick,soul-sand,sponge,wool-colors-other-than-0,snow-block-high-light-melt,snow-layer-melt,gravity,native-render,state-collision-light -->
<!-- frozen-trace=96b4832d7aad487642ff290b7858f4385b0b54b76755a9878c49bda4182ef467 -->

# Beta 1.7.3 terrain-crafted solid bounded stability envelope

Eight caller-owned rows cover stone, dirt, wood planks, sandstone, white wool, bookshelf, the
crafting table, and snow block. These remaining inert solids sit outside the occupied mineral
stability cycle. One maintained public TestKit family gameplay-places each block beneath a
direct stone overhead, observes it across two hundred official server ticks, removes the
overhead through normal diamond-pickaxe breaking, observes forty more ticks, and crosses a
clean save plus fresh login.

The tick window is an intentional bounded observation contract, not an asynchronous sleep. The
overhead transition is causal: the official server removes an adjacent block and emits its native
neighbor notification before the target is checked. Together the rows execute sixteen Functional
Census observations inside one coherent stability mini-subsystem. Fifteen claims are verified.
Snow block's dark bounded survival is only partial tick-policy evidence because this fixture does
not exercise its scheduled high-light melt branch; its neighbor-response claim remains verified.

Occupied cobblestone, ores, mineral blocks, bricks, mossy cobblestone, obsidian, clay, and
netherrack remain outside this family. Grass and redstone-ore random ticks, soul-sand, sponge,
wool colors other than metadata zero, snow-block high-light melt, snow-layer melt, gravity, native rendering, and the
state, collision, and light envelopes are owned by other packages. This map does not claim
arbitrary neighbor identities, longer time windows, or unbounded invariance.

Frozen signal:
`provider=b1.7.3-server-lifecycle,family=terrain-crafted-solid-stability-envelope,rows=8,passed=8,claims=16,tick-window=200,neighbor=stone-overhead-remove,reload=FRESH_LOGINx8,evidence=700cebd5652c67a0242324fa6a15d4871223ca48e0293229735ec7377844b60f,isolation=8-fresh-worlds`.

Frozen trace:
`v1|server=official-b1.7.3|seed=17320110707|provider=b1.7.3-server-lifecycle|family=terrain-crafted-solid-stability-envelope|rows=stone-bounded-stability-envelope,dirt-bounded-stability-envelope,wood-planks-bounded-stability-envelope,sandstone-bounded-stability-envelope,white-wool-bounded-stability-envelope,bookshelf-bounded-stability-envelope,crafting-table-bounded-stability-envelope,snow-block-bounded-stability-envelope|actions=place+tick-window200+remove-stone-overhead+fresh-login|oracle=canonical-public-block-stability-evidence|evidence=700cebd5652c67a0242324fa6a15d4871223ca48e0293229735ec7377844b60f`.

The two independent official passes produce the same canonical evidence hash,
`700cebd5652c67a0242324fa6a15d4871223ca48e0293229735ec7377844b60f`. The frozen semantic
trace is bound by signature
`96b4832d7aad487642ff290b7858f4385b0b54b76755a9878c49bda4182ef467`.
