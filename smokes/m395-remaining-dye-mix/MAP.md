<!-- worldline-map-schema=1 -->
<!-- boundary=m395-remaining-dye-mix -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=1ba82fec7effc4370c0a4169136f177851484511a3b86bf1d2aaf76134e1491c -->

# M395 behavior map

Eighteen accepted window-0 Packet102 clicks mix remaining dyes in the
personal 2x2 grid. Cactus green `351:2` plus lapis `351:4` yield cyan
`351x2:6`, rose red `351:1` plus bone meal `351:15` yield pink `351x2:9`,
and lapis `351:4` plus bone meal `351:15` yield light blue `351x2:12`. The
official server accepts those predicted mixed 351 damages. The three stacks
survive a clean save plus fresh login.

This map does not claim primary dye milling (M328), orange/purple/lime mixes
(M348), dyed wool `35` (M315, M368), or wool placement (M197, M248-M287).

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=green351:2+lapis351:4+red351:1+bonemeal351:15+lapis351:4+bonemeal351:15|cause=packet102-window0-2x2-shapeless|wire=packet106-accepted|oracle=three-remaining-mixed-dye-damages+fresh-login|inputs=351:2+351:4+351:1+351:15+351:4+351:15,results=351x2:6+351x2:9+351x2:12,grid=2x2,actions=18,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`1ba82fec7effc4370c0a4169136f177851484511a3b86bf1d2aaf76134e1491c`.
