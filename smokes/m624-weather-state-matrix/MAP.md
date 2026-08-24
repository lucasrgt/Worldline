<!-- worldline-map-schema=1 -->
<!-- boundary=state-world-differential -->
<!-- nonclaims=packet-broadcasts,lightning-entities,biome-effects,arbitrary-rng-seeds -->
<!-- frozen-trace=b96fb0662d743ed48b06c1346b4f18210823c33353aeed64b310a19db86892ad -->

# M624 weather state matrix behavior map

## Boundary

This milestone constructs five identical weather countdown cases against
mapped and official Beta 1.7.3 server classes: dry-to-rain, rain-to-dry,
calm-to-thunder, thunder-to-calm, and simultaneous dry/calm-to-storm.
Each case advances exactly three protected `World.updateWeather()` calls.

The trace records rain/thunder flags, countdowns, and their millistrength ramps
before and after every update. The final `WorldInfo` is serialized through its
official NBT representation and reconstructed; all persisted weather fields
must remain identical.

## Mapping anchors

- `World` maps to client `fd` and server `dj`; `updateWeather()` maps to client
  `m` and server `i`; rain/thunder strengths are server fields `j` and `l`.
- `WorldInfo` maps to client `ei` and server `ct`; the server rain flag/time
  accessors are `l`/`b` and `m`/`c`, while thunder uses `j`/`a` and `k`/`b`.
- `NBTTagCompound` maps to client `nu` and server `iq`.

## Oracle independence

The mapped subject compiles against RetroMCP's mapped server classes. The
official oracle subclasses only obfuscated `dj` and compiles directly against
the hash-verified official server JAR. They share only `CanonicalTrace`, the
seed, and literal initial flags/countdowns.

## Pass condition

Two mapped and two official processes must be deterministic within each pair
and byte-identical across the mapping boundary. All four individual toggles and
the simultaneous toggle must occur at their exact countdown boundary. Reseeded
countdowns must stay within the official state-specific bounds, strengths must
follow the exact trace, and NBT round-trips must preserve the final flags and
countdowns.

This milestone does not claim packet broadcasts, lightning spawning, biome
effects, client rendering, precipitation collision, or other RNG seeds.

## Frozen semantic signal

`official oracle: MATCH`
