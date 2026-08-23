<!-- worldline-map-schema=1 -->
<!-- boundary=m268-flint-steel-fire -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=50fbd4ba9248b6647eee949cc037cb741948628611a039361dfe320c5099dc22 -->

# M268 behavior map

Packet15 of flint-and-steel item `259` on a raised stone column places fire
`51:0` in the air cell above. After that exact live placement, vanilla may
retain or extinguish unsupported stone fire during the bounded hold and before
a fresh login. Both observations must be either fire `51:0` or air `0:0`, and
the stone support must persist. This is distinct from M151 netherrack keeping
flame and from M152 wool consumption.

Frozen trace:

```text
v2|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+flintsteel259|cause=packet15-item259|wire=packet53-fire51:0|oracle=live-place+bounded-live-fire-or-air+fresh-login-fire-or-air|nonclaim=stone-fire-persistence|column=17,support=4:71:4:1:0,fire=4:72:4:51:0,fresh=fire-or-air,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`50fbd4ba9248b6647eee949cc037cb741948628611a039361dfe320c5099dc22`.
