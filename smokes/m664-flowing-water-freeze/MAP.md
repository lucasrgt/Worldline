<!-- worldline-map-schema=1 -->
<!-- boundary=flowing-water-freeze -->
<!-- nonclaims=client-rendering,arbitrary-biomes,flowing-water-levels-0-and-2-through-7,fluid-spread,source-regeneration -->
<!-- frozen-trace=802f64957011360c097c5fea406200e665523eb5038aee5baec7f1158f3d1ede -->

# M664 semantic map

Evidence: official-server dual replica.

The public boundary is `worldline.testkit.FlowingWaterFreezeFixture#verify`. It advances a
bounded native ambient scheduler over adjacent cold-biome cells, requires block light below ten,
requires still water `9:0` to become ice `79:0`, and requires the flowing control to remain
flowing water `8:1` on every pass. Evidence includes the attempt ceiling and stable light values,
not the successful random pass or fixture coordinates.

The mapped path subclasses `World` and invokes protected `doRandomUpdateTicks()`. The official
path subclasses server `dj` and invokes its protected counterpart `j()`. Both execute the native
random selection, cold-biome query, light boundary, water-state test, and ice replacement; neither
calls a direct ice placement. The only fixture write after chunk loading is the initial flowing
water control state.

Seed `1772835215` is bounded to the active 19-by-19 chunk radius. In-memory chunks provide a
shared stone floor and still-water surface, then the selected adjacent cell is changed to flowing
water `8:1` before either scheduler starts. Four fresh processes emit one identical canonical
trace.

Frozen signal: `still=9:0->79:0,flowing=8:1->8:1,light<10,oracle=MATCH`.

Frozen semantic SHA-256: `802f64957011360c097c5fea406200e665523eb5038aee5baec7f1158f3d1ede`.
