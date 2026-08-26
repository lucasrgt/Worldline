<!-- worldline-map-schema=1 -->
<!-- boundary=powered-creeper -->
<!-- nonclaims=explosion-strength,explosion-radius,fuse,gunpowder,natural-thunder-scheduling -->
<!-- frozen-trace=a8d0b1a2e0ef160405005b422d0e4d7fce5c1ba11da765bd09f9017a1914539e -->

# M659 powered creeper behavior map

## Boundary

The fixture joins one official creeper at cell 8:65:8 and records `getPowered() == false`
before lightning exists. It then constructs and joins one native lightning entity at the exact
creeper coordinates. Both entities must be present in the official world, the lightning must be
alive, and its integer cell must equal the creeper cell before the native lightning callback runs.

The public `PoweredCreeperActions` boundary performs that causal strike and records process-local
entity IDs in the trial. `PoweredCreeperEvidence` accepts the result only when the unpowered
prerequisite, immediate powered state, and one-tick held powered state all preserve the original
creeper identity and cell. Equatable evidence normalizes raw IDs into the stable same-identity fact.

## Official symbol anchors

- `EntityCreeper` maps to client `gb` and server `ec`.
- `EntityCreeper.getPowered()` maps to `s()` on both sides.
- `Entity.onStruckByLightning(EntityLightningBolt)` maps to `a(...)` on both sides.
- `EntityLightningBolt` maps to `c` on both sides.
- `Entity.entityId` maps to client `aD` and server `aG`; position maps to `aM/aN/aO`
  and `aP/aQ/aR` respectively.

Every six-column symbol row is copied from the frozen RetroMCP mapping with empty client or server
sides retained as empty TAB fields. The mapped subject and official-name oracle share only literals,
the trace schema, and semantically equivalent public observations.

## Distinction and nonclaims

M589 proves lightning fire creation, while M391/M448/M456/M638 prove creeper fuse, cancellation,
or explosion boundaries. M659 claims only lightning-driven charge state on the same creeper. It does
not claim natural thunder scheduling, fuse timing, explosion strength or radius, or gunpowder drops.
The later unstarted milestone name `m673-powered-creeper-lightning` is not used as a public token.

Frozen signal:

```text
oracle=MATCH,fixture=unpowered-creeper-observed-lightning,powered=same-identity,held=one-tick,explosion=unclaimed
```

Frozen semantic SHA-256:
`a8d0b1a2e0ef160405005b422d0e4d7fce5c1ba11da765bd09f9017a1914539e`.
