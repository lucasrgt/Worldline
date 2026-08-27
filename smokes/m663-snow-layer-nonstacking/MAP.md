<!-- worldline-map-schema=1 -->
<!-- boundary=snow-layer-nonstacking -->
<!-- nonclaims=client-precipitation-rendering,snow-depth-growth,arbitrary-biomes,successful-rng-pass,unbounded-weather -->
<!-- frozen-trace=547c356d71192c531d2eeed214191d6ff6d0aa90d6eff2f8317250e55199124c -->

# M663 snow layer nonstacking behavior map

## Boundary

The official fixture builds identical solid surfaces in a cold biome, primes
rain, and advances the native ambient scheduler until it observes snow layer
`78:0`. It then advances eight further wet passes. The first layer must remain
`78:0`, the cell above it must remain air, and the target column must contain
exactly one snow layer. An identical dry control remains air throughout.

The public boundary is `worldline.testkit.SnowLayerNonstackingFixture#verify`.
Its equatable evidence retains only the formation and continuation attempt
ceilings plus block light; it does not retain the random coordinate or the
successful random pass.

The mapped and official paths both invoke the native ambient scheduler after
weather priming. Neither path directly places the snow layer after
initialization.

Frozen trace:

```text
v1|seed=1772835215|formed:time=0,entities=0,column=0.78.0.0.1.1.16.0|settled:time=0,entities=0,column=78.0.0.1.1.8.0
```

Frozen semantic signal: `official oracle: MATCH`.

Frozen semantic SHA-256:
`547c356d71192c531d2eeed214191d6ff6d0aa90d6eff2f8317250e55199124c`.
