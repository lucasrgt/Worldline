<!-- worldline-map-schema=1 -->
<!-- boundary=m396-remaining-wool-crafts -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=7bd1423c0f7af5c289a638d55eb9b16ec8b709217f849b00e95b0a3316990c54 -->

# M396 behavior map

Eighteen accepted window-0 Packet102 clicks dye white wool `35:0` with
magenta `351:13`, light blue `351:12`, and lime `351:10` in the personal
2x2 grid. The official server accepts the predicted colored-wool results
`35:2`, `35:3`, and `35:5`. Those exact stacks survive a clean save plus
fresh login.

This map does not claim M315 rose-red/cactus-green/lapis hashes
(`351:1/2/4` -> `35:14/13/11`), M368 yellow/orange/pink hashes
(`351:11/14/9` -> `35:4/1/6`), or wool placement (M197, M248-M287).

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=white-wool35:0+dyes351-remaining|cause=packet102-window0-2x2-shapeless|wire=packet106-accepted|oracle=three-remaining-wool-damages+fresh-login|wool=35:0,dyes=351:13+351:12+351:10,results=35:2+35:3+35:5,grid=2x2,actions=18,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`7bd1423c0f7af5c289a638d55eb9b16ec8b709217f849b00e95b0a3316990c54`.
