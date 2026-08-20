# M348 behavior map

Eighteen accepted window-0 Packet102 clicks mix primary dyes in the personal
2x2 grid. Rose red `351:1` plus yellow `351:11` yield orange `351x2:14`,
rose red `351:1` plus lapis `351:4` yield purple `351x2:5`, and cactus
green `351:2` plus bone meal `351:15` yield lime `351x2:10`. The official
server accepts those predicted mixed 351 damages. The three stacks survive
a clean save plus fresh login.

This map does not claim primary dye milling (M328), dyed wool `35` (M315),
or wool placement (M197, M248-M287).

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=red351:1+yellow351:11+red351:1+lapis351:4+green351:2+bonemeal351:15|cause=packet102-window0-2x2-shapeless|wire=packet106-accepted|oracle=three-mixed-dye-damages+fresh-login|inputs=351:1+351:11+351:1+351:4+351:2+351:15,results=351x2:14+351x2:5+351x2:10,grid=2x2,actions=18,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`2c8b97b5aa9c68fef810b33465f38d10146adbfcea7c9994c7742c0ae1305b94`.
