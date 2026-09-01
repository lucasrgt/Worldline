<!-- worldline-map-schema=1 -->
<!-- boundary=b173-cake-serving-lifecycle-cycle -->
<!-- nonclaims=selection-box,client-render,cake-crafting,breaking-cake-directly,drop-randomness,arbitrary-invalid-metadata -->
<!-- frozen-trace=18c34cfdbe2bf571425ca2cff6e9853721f79a022593cc45fb47e969591c13b9 -->

# Beta 1.7.3 cake serving lifecycle

The public `CakeServingFixture` executes one coherent serving subsystem. An
official player places cake item `354`, consumes all six servings, and observes
the reachable `92:0` through `92:5` metadata states followed by air. Every
serving restores exactly three health points from the seeded `1`-health
baseline.

The same causal run samples a fixed server-authoritative movement lane at each
metadata value. The lane is blocked by the first two shapes and becomes
passable for the remaining four, proving that collision changes with the
serving state. Every remaining state emits block light `0` and transports sky
light `15`. Metadata `3` holds for 200 ticks and survives a clean save plus
fresh login. After the first cake is fully consumed, a second gameplay-placed
cake is invalidated when the player breaks its observed stone support; final
air survives another fresh login.

The map does not claim the client selection box, rendering, cake crafting,
direct cake-breaking semantics, randomized drops, or unreachable metadata.

Frozen aggregate signal:
`family=cake-serving-lifecycle,claims=5,states=7,servings=6,health=1->4->7->10->13->16->19,collision=CCUUUU,light=0:15x6,tick=200,reload=FRESH_LOGINx2,support=92:0->0:0,evidence=a8d8417877edfda637e30331059950bb257ca561581e214c9b271eb8ece54f79`.

Qualified semantic signature:
`18c34cfdbe2bf571425ca2cff6e9853721f79a022593cc45fb47e969591c13b9`.
