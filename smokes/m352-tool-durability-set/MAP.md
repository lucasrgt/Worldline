<!-- worldline-map-schema=1 -->
<!-- boundary=m352-tool-durability-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=46cbf98b50d0745eafee30276fb3d3adafbbd1381f71bf7106012dbe80b75a30 -->

# M352 behavior map

The M208 raised stone column hosts cobble `4:0` and stone `1:0` in one
session. Packet14 while holding wooden pickaxe `270` breaks cobble to
air. Packet14 while holding iron pickaxe `257` breaks stone to air.
Packet14 while holding gold pickaxe `285` breaks a second cobble to air.
Each held stack remains with official remaining durability damage `1`,
which persists across a clean save plus fresh login.

This map does not re-qualify M300 ore-and-cobble Packet21 drops. Headless
`B173WireClient` only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+cobble4+stone1+cobble4|cause=packet14-woodpick270+ironpick257+goldpick285|wire=packet53-air+packet103-270:1+257:1+285:1|oracle=held-stack-durability+fresh-login|column=17,support=4:71:4:1:0,cobble=4:72:4:4:0->0:0,stone=5:71:4:1:0->0:0,goldcobble=3:71:4:4:0->0:0,wood=270:1,iron=257:1,gold=285:1,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`46cbf98b50d0745eafee30276fb3d3adafbbd1381f71bf7106012dbe80b75a30`.
