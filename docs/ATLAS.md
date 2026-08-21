# Seed Atlas Contract

## Scope

Renders any Beta 1.7.3 seed as a self-contained top-down terrain map:
boots an unmodified official dedicated server under the requested seed,
walks the player in bounded steps across every chunk of the requested area,
decodes each streamed chunk, and emits a deterministic HTML page with one
colored cell per column plus a block-id legend.

## Command

```text
worldline atlas <seed> <radius-1..4> <output.html>
```

Radius covers `(2r+1)^2` chunks (radius 1 = 48x48 columns). The page embeds
no scripts; colors are pure functions of legacy block ids; identical seeds
produce byte-identical pages. Exit 0 on success, 1 on capture failure.

## Boundary

`worldline.analysis.AtlasRunner` is the neutral contract;
`B173AtlasRunner` drives `B173DedicatedServer` + `B173WireClient`
(the same wire path proven by the fixed-seed-terrain milestone), moving in
<=4-block glides so the vanilla anti-cheat never trips, and sampling the
top non-air block per column.

## Non-claims

No biome/structure overlays, Nether/end dimensions, cave rendering, or
cross-version seed compatibility. Radius is bounded by server view streaming.