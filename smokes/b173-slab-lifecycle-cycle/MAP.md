<!-- worldline-map-schema=1 -->
<!-- boundary=b173-slab-lifecycle-cycle -->
<!-- nonclaims=slab-combination,upper-half-placement,collision,native-render -->
<!-- frozen-trace=62cff6dd83cf8f4a465c699fe79b75925d3cf26450ce6cafd6b6dc7228865674 -->

# Beta 1.7.3 slab state lifecycles

Five public TestKit rows execute the complete isolated lifecycle of stone, sandstone, wood, and
cobblestone single slabs plus the double stone slab. The four item `44` damage values must produce
the matching `44:0..3` block states; item `43` must produce `43:0`. Every state survives a fresh
login before official Packet14 harvesting removes it and emits its exact historical drop.

The four single-slab cases deliberately share one census subject and its four claims. Their purpose
is to prove the complete reachable item-metadata family rather than inflate the milestone count.
The double slab is a separate registered subject and must disassemble to two distinct dropped-item
entities, each carrying one stone slab, rather than one count-two stack.

This map does not claim combining two slabs, upper-half placement, collision, recipes, silk touch,
particles, or native rendering.

Discovery signal:
`provider=b1.7.3-server-lifecycle,family=slab-state,rows=5,passed=5,layers=U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A,reload=FRESH_LOGINx10,evidence=2cf5cffcedc9cf3c585c6184e7af894eb0d1459ed0b3ff7b7aafc7737ef3a6d2,isolation=5-fresh-worlds`.
