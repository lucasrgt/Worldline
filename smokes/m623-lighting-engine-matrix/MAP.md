<!-- worldline-map-schema=1 -->
<!-- boundary=state-world-differential -->
<!-- nonclaims=arbitrary-terrain,chunk-seams,day-night-brightness,client-rendering -->
<!-- frozen-trace=b1bf8088d22536e61795483c655bc8e1d82eed18d0e6298e0859aa40525435fb -->

# M623 lighting engine matrix behavior map

## Boundary

This milestone constructs the same flat in-memory world against mapped and
official Beta 1.7.3 server classes. Initial chunk generation includes one
solid five-by-five roof and one five-by-five roof with a central skylight
aperture. The fixture samples their saved sky and block light.

The update matrix replaces one air cell successively with glowstone, an active
furnace, glowing redstone ore, an active redstone torch, and air. It drains the
official lighting queue after each replacement and records source plus three
horizontal attenuation samples. A final update closes and reopens the roof
aperture and requires exact skylight recovery.

## Mapping anchors

- `World` maps to client `fd` and server `dj`; `setBlockWithNotify` maps to
  client `f` / server `e`, and `getSavedLightValue` is `a` on both sides.
- The server lighting queue drain `func_6156_d()` maps to `f`.
- `EnumSkyBlock` maps to client `eb` and server `co`; `Sky` and `Block` are
  `a` and `b`.

## Oracle independence

The mapped subject compiles against RetroMCP's mapped server classes. The
official oracle uses only obfuscated symbols and compiles directly against the
hash-verified official server JAR. They share only `CanonicalTrace`, the seed,
literal source IDs, and literal fixture geometry.

## Pass condition

Two fresh mapped processes and two fresh official-oracle processes must be
deterministic within each pair and byte-identical across the mapping boundary.
Generated open/aperture skylight must be 15 while the roofed sample is darker.
Each source must match its emission and first attenuation level; removal must
clear the sampled block light. Closing the aperture must darken its sample and
reopening it must restore the original value.

This milestone does not claim arbitrary geometry, chunk-seam propagation,
weather/day brightness, client rendering, or asynchronous server scheduling.

## Frozen semantic signal

`official oracle: MATCH`
