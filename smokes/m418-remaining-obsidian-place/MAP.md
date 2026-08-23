<!-- worldline-map-schema=1 -->
<!-- boundary=m418-remaining-obsidian-place -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=7c15aa18aedb3ac5e34f9b7fbc2836311b51f88fc0737ed40298e3d3e65be80e -->

# M418 behavior map

Packet15 places four obsidian item `49` cells as an L-shaped portal-frame
fragment on the raised stone column: two bottom cells plus two left-pillar
cells. Interior air stays air. Flint-and-steel is absent, so portal `90`
never appears. Packet16 then holds diamond pickaxe `278` and Packet14 fully
breaks the cap `49` cell to air, emitting Packet21 obsidian `49:1:0`. The
other three fragment cells remain `49:0`.

This map does not re-qualify M216 (one obsidian Packet15), M132 portal
activation, or sibling M382 fourteen-cell lit `4x5` frame plus portal `90`.
It reuses the existing Packet21 tracker. Headless `B173WireClient` only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+obsidian49-frame-fragment4|construction=packet15-four-obsidian49|baseline=air-interior-unlit|cause=packet14-diamondpick278|wire=packet53-air+packet21-id49|oracle=unlit-obsidian-frame-fragment+pick-harvest49|column=17,support=4:71:4:1:0,frame=5:71:4+6:71:4+5:72:4+5:73:4,obsidian=4x49,portal=90:absent,base=5:71:4:49:0,east=6:71:4:49:0,pillar=5:72:4:49:0,cap=5:73:4:49:0->0:0,pick=278,drops=packet21-49,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`7c15aa18aedb3ac5e34f9b7fbc2836311b51f88fc0737ed40298e3d3e65be80e`.
