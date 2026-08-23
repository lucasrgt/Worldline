<!-- worldline-map-schema=1 -->
<!-- boundary=m328-dye-family-crafts -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=7ae29bcd82b147e1286ec7a3b4655087822ac5f5379f18142eab3fd163dda815 -->

# M328 behavior map

Eighteen accepted window-0 Packet102 clicks mill dye powders in the personal
2x2 grid. Bone `352` yields bone meal `351x3:15`, rose `38` yields rose red
`351x2:1`, and dandelion `37` yields yellow `351x2:11`. Ink sac `351:0` plus
seeded bone meal `351:15` mix to gray dye `351x2:8`. The official server
accepts those predicted 351 damages. The four stacks survive a clean save
plus fresh login.

This map does not claim dyed wool `35` (M315) or wool placement
(M197, M248-M287).

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=bone352+rose38+dandelion37+ink351:0+bonemeal351:15|cause=packet102-window0-2x2-shapeless|wire=packet106-accepted|oracle=four-dye-damages+fresh-login|inputs=352:0+38:0+37:0+351:0+351:15,results=351x3:15+351x2:1+351x2:11+351x2:8,grid=2x2,actions=18,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`7ae29bcd82b147e1286ec7a3b4655087822ac5f5379f18142eab3fd163dda815`.
