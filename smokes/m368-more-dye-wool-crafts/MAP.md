# M368 behavior map

Eighteen accepted window-0 Packet102 clicks dye white wool `35:0` with
dandelion yellow `351:11`, orange `351:14`, and pink `351:9` in the personal
2x2 grid. The official server accepts the predicted colored-wool results
`35:4`, `35:1`, and `35:6`. Those exact stacks survive a clean save plus
fresh login.

This map does not claim M315 rose-red/cactus-green/lapis hashes
(`351:1/2/4` -> `35:14/13/11`) or wool placement (M197, M248-M287).

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=white-wool35:0+dyes351-more|cause=packet102-window0-2x2-shapeless|wire=packet106-accepted|oracle=three-new-wool-damages+fresh-login|wool=35:0,dyes=351:11+351:14+351:9,results=35:4+35:1+35:6,grid=2x2,actions=18,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`af97665c706b12d71c1c228a931a7efec0c18fda505b259de31fdf174b8a17b9`.
