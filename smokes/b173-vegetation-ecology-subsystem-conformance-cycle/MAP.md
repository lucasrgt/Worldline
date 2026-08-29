# Beta 1.7.3 vegetation ecology subsystem conformance

This milestone binds six native ecology subjects to one reusable public TestKit fixture. It closes
only the nineteen Functional Census cells declared in `census-delta.tsv` and keeps already-qualified
random ticks, lifecycle, drops, persistence, rendering, tall-grass physics, leaf lighting, and cane
support loss under their existing evidence owners.

The mapped runtime and untouched official server both prepare legal substrates for grass, saplings,
leaves, tall grass, crops, and sugar cane. They enumerate the bounded native metadata families,
compare full-cube and passable collision, inspect the claimed opacity/emission table rows, preserve
grass under a neighbor event, remove sapling and crop support, and mark leaves for decay by
removing an adjacent log.

Canonical trace:

`v1|seed=17320110870|vegetation-2:time=1,entities=0,column=2.1.1.25500.1.0|vegetation-6:time=2,entities=0,column=6.1799.0.0.2.0|vegetation-18:time=3,entities=0,column=18.30583.1.-1.3.8|vegetation-31:time=4,entities=0,column=31.7.-1.-1.-1.0|vegetation-59:time=5,entities=0,column=59.255.0.0.2.0|vegetation-83:time=6,entities=0,column=83.65535.0.0.-1.0`

Frozen signature:

`ba9cade29f551f43834d9727c839183fe61dd288fef93e0ef1b4b5caab1f7913`

Frozen semantic signal:

`family=vegetation-ecology,subjects=2+6+18+31+59+83,claims=19,states=6,shapes=5,light=4,neighbors=4,oracle=MATCH`

Atlas facets: ecology, vegetation, growth, support-dependent, stateful-metadata, collision,
lighting, neighbor-response, archetype, server-headless, official-oracle, public-testkit.

Nonclaims: this cycle does not claim random growth timing, tree generation shape, leaf decay
completion or drops, crop harvest yield, sugar-cane growth cadence, native rendering, placement,
break, save/reload, or any state outside the explicitly enumerated Beta 1.7.3 domains.
