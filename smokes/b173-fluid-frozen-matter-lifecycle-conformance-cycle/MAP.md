<!-- worldline-map-schema=1 -->
<!-- boundary=b173-fluid-frozen-matter-lifecycle-conformance-cycle -->
<!-- nonclaims=registry-presence,save-reload,light-behavior,native-render,fluid-flow-cadence,fluid-reactions,ambient-freezing,ambient-melting -->
<!-- frozen-trace=8000df9fba5c971bba329c278687510456064cba2dced8f9e4c2e1b552a89d97 -->

# Beta 1.7.3 fluid and frozen-matter lifecycle

This milestone binds water, lava, sponge, snow layer, ice, and snow block to one reusable public
TestKit mini-subsystem. It closes only the twenty-one Functional Census cells declared in
`census-delta.tsv`; existing flow cadence, persistence, lighting, rendering, reaction, formation,
and ambient-melt evidence keeps its original owner.

The universal layer exercises supplied moving-fluid item routes and the native server break
sequence. The archetype layer proves empty drops for all four fluid IDs, the complete flowing-lava
metadata domain, snow and ice physical/tick behavior, and stable neighbor callbacks. The singular
sponge layer proves its native tick and neighbor no-op boundaries. Snow-layer support removal is
causal: the supporting stone is observed before removal, then the native neighbor callback owns
the transition to air. Ice is harvested over solid support and the native
callback owns its water replacement without an item drop.

Canonical trace:

`v1|seed=17320110872|fluid-frozen-8:time=1,entities=0,column=8.1.-1.1.-1.-1.-1|fluid-frozen-9:time=2,entities=0,column=9.-1.-1.1.-1.-1.-1|fluid-frozen-10:time=3,entities=0,column=10.1.65535.1.-1.-1.-1|fluid-frozen-11:time=4,entities=0,column=11.-1.-1.1.-1.-1.-1|fluid-frozen-19:time=5,entities=0,column=19.-1.-1.-1.1.1.-1|fluid-frozen-78:time=6,entities=0,column=78.-1.-1.-1.1.1.1|fluid-frozen-79:time=7,entities=0,column=79.-1.1.-1.1.1.1|fluid-frozen-80:time=8,entities=0,column=80.-1.-1.-1.1.-1.-1`

Frozen signature:
`8000df9fba5c971bba329c278687510456064cba2dced8f9e4c2e1b552a89d97`.

Frozen semantic signal:
`family=fluid-frozen-matter,subjects=8+9+10+11+19+78+79+80,claims=21,fluids=11,sponge=2,snow-layer=2,ice=5,snow-block=1,oracle=MATCH`.

Atlas facets: fluids, frozen-matter, phase-transition, item-route, empty-drop, metadata-domain,
collision, scheduled-tick, random-tick, neighbor-response, support-dependent, fluid-reactive,
universal, archetype, singular, server-headless, official-oracle, public-testkit.

Nonclaims: this cycle does not claim source regeneration, flow topology or cadence, water-lava
reactions, entity currents or damage, sponge absorption, chunk persistence, light propagation,
ambient ice/snow formation, ambient melting, or native client rendering.
