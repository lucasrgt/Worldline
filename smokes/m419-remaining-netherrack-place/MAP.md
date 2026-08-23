<!-- worldline-map-schema=1 -->
<!-- boundary=m419-remaining-netherrack-place -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=c7dec53dcc70e1baa573a851f8e296853cfe16d36ddd182d1cfd5e83a8a4dea7 -->

# M419 behavior map

Official server symbols:

- `net.minecraft.src.WorldProviderHell` is dimension `-1`. M130 pre-login
  player NBT with `Dimension: -1` plus `allow-nether=true` is the entry.
- `net.minecraft.src.ItemBlock.onItemUse` Packet15-places netherrack `87`,
  soul sand `88`, and glowstone `89` on the UP face of a natural
  netherrack platform. That is a Nether terrain family, not a single
  netherrack cell.
- The live cache and a fresh login show all three cells.

This map does not re-run M224/M192/M191 Overworld 1:1 places, M357
glowstone-dust crafts, M343 fire-on-netherrack, or M382 portal lighting.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|profile=allow-nether-true|entry=prelogin-player-nbt-dimension-minus-one+item87+item88+item89|fixture=nether-netherrack87-platform+packet15-87+88+89|cause=packet15-item87+item88+item89|wire=packet53-netherrack87+soulsand88+glowstone89|oracle=nether-terrain-family-place+fresh-login|dimension=-1,support=9:9:7:87:0,netherrack=10:10:7:87:0,soulsand=8:10:7:88:0,glowstone=9:10:8:89:0,persisted=true,clients=3,disconnect=clean
```

Frozen semantic SHA-256:
`c7dec53dcc70e1baa573a851f8e296853cfe16d36ddd182d1cfd5e83a8a4dea7`.
