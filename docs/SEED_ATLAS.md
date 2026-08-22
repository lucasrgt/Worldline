# Seed Atlas Contract

## Scope

Renders any Beta 1.7.3 seed as a self-contained top-down terrain map: boots an
unmodified official dedicated server under the requested seed, walks the player
in bounded steps across every chunk of the requested area, decodes each streamed
chunk, and emits deterministic HTML with one colored cell per column and a
block-ID legend.

## Command

```text
worldline atlas <seed> <radius-1..4> <output.html>
```

Radius covers `(2r+1)^2` chunks. The page embeds no scripts; colors are pure
functions of legacy block IDs; identical seeds produce byte-identical pages.

## Boundary

`worldline.analysis.AtlasRunner` is the neutral contract. `B173AtlasRunner`
lives in the optional adapter source set and drives the official dedicated
server through the same wire path proven by the fixed-seed terrain milestone.

## Non-claims

No biome or structure overlays, Nether rendering, cave rendering, or
cross-version seed compatibility. Radius is bounded by server view streaming.
