<!-- worldline-map-schema=1 -->
<!-- boundary=m598-gravel-fall-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=18cfe17d0447d6e4b77b09df02092118df5f6b7ed58ee4697eb9882468eed37d -->

# M598 behavior map

The official fixture stabilizes stone `1:0` at `(4,64,4)` with gravel `13:0`
directly above it. Packet14 removes the stone and Packet53 must expose air
at the lower coordinate before settlement.

The live Packet23 object type is recorded from the official oracle rather
than assumed from decompiled source. That observed type is `71`, the
falling-gravel entity, not sand type `70`. The server then places `13:0`
in the lower cell and leaves `0:0` in the former upper cell. After clean
disconnect/save, a fresh Packet51 must expose both states.

Vanilla EntityFallingSand drops the block as an item when
`canBlockBePlacedAt` fails; this SET freezes the landing-place outcome
and the observed entity type. It does not claim sand type `70`, flint
`318`, long falls, or piston interaction.

This map is distinct from M119/M597 sand, M274 block-only gravel settle,
and M342's sand-plus-gravel pair.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=stone-column+supported-gravel13|settle=40+40ticks|cause=packet14-remove-support|confirmation=packet53-air|effect=official-falling-gravel-entity-land|observation=packet23-type-observed+live-packet53+fresh-login-packet51|column=10,lower=4:64:4:1:0->13:0,upper=4:65:4:13:0->0:0,entity-type=71,packet23=71,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`18cfe17d0447d6e4b77b09df02092118df5f6b7ed58ee4697eb9882468eed37d`.
