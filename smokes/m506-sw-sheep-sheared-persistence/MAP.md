<!-- worldline-map-schema=1 -->
<!-- boundary=state-world-differential -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=57aca1de84ec46a162610d18a48ba190b6128e62cc628d70e9e6ef92361790bd -->

# M506-SW behavior map

The official Beta 1.7.3 `EntitySheep` data watcher stores color in the low
nibble of index `16` and `Sheared` in bit `16`. A red sheep therefore changes
from metadata `14` to `30` when sheared, while an untouched white control is
`0`. The official NBT compound persists `Color` and boolean `Sheared`.

The smoke dyes and shears one of two live sheep, saves, and restarts. Metadata
`30` survives, the white control remains `0`, and interacting with the already
sheared sheep emits no second wool entity. A smoke-only semantic NBT mutation
changes exactly one red sheep from `Sheared=1` to `Sheared=0`; after restart it
appears as metadata `14` and can be sheared back to `30` with a new red-wool
Packet21.

The official class has no grass-grazing update or wool-regrowth state. This
map does not claim either behavior, natural spawning, breeding, or a public
entity-state API.

Frozen semantic SHA-256:
`57aca1de84ec46a162610d18a48ba190b6128e62cc628d70e9e6ef92361790bd`.

## Frozen semantic signal

`column=17,platform=5x5-grass,spawners=4:72:4+5:72:4,mobs=type91+type91,red=14->30,persisted=30,control=0,repeat=no-new-wool,nbt.Sheared=1->0,mutated=14->30,changed=1,clients=3,restarts=3`
