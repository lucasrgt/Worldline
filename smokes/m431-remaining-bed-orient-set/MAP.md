<!-- worldline-map-schema=1 -->
<!-- boundary=m431-remaining-bed-orient-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=8aa709e05da8be4a281e9eded3c6297e0f4236a515d73d60178570c69cf303a1 -->

# M431 behavior map

Official server symbols:

- `net.minecraft.src.ItemBed.onItemUse` places block `26` only on face `1`
  (UP). `MathHelper.floor_double((yaw * 4 / 360) + 0.5) & 3` writes the foot
  metadata and head metadata `+8` one cell along that facing.
- Yaw `90` writes west foot `26:1` and head `26:9` one cell west. Yaw `180`
  writes north foot `26:2` and head `26:10` one cell north. Yaw `-90` writes
  east foot `26:3` and head `26:11` one cell east.
- Both halves must be air and both supports must be solid cubes. This map
  does not occupy the bed, skip night, or enter the Nether.

This map does not re-run M240 south `26:0`/`26:8`, M330 Packet17 sleep, or
M359 Nether Packet60 explode. Headless `B173WireClient` protocol-14 only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+item355-block26-west+north+east|cause=packet15-item355+look90+look180+look-90|wire=packet53-bed26:1/9+26:2/10+26:3/11|oracle=remaining-foot-head-facings+fresh-login|dimension=0,column=17,support=4:71:4:1:0,west=3:72:4:26:1+2:72:4:26:9,north=4:72:3:26:2+4:72:2:26:10,east=5:72:4:26:3+6:72:4:26:11,look=90+180+-90,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`8aa709e05da8be4a281e9eded3c6297e0f4236a515d73d60178570c69cf303a1`.
