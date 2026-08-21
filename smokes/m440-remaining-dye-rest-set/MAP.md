# M440 behavior map

Twenty accepted window-0 Packet102 clicks mix remaining dyes in the
personal 2x2 grid. Ink sac `351:0` plus two bone meal `351:15` yield light
gray `351x3:7`, gray `351:8` plus bone meal `351:15` yield light gray
`351x2:7`, and purple `351:5` plus pink `351:9` yield magenta `351x2:13`.
The official server accepts those predicted remaining 351 damages. The
three stacks survive a clean save plus fresh login.

This map does not claim primary dye milling (M328), orange/purple/lime
mixes (M348), cyan/pink/light-blue mixes (M395), dyed wool `35` (M315,
M368, M396), or wool placement (M197, M248-M287). Cocoa beans `351:3`
have no remaining 351 mix recipe.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=ink351:0+bonemeal351:15+gray351:8+purple351:5+pink351:9|cause=packet102-window0-2x2-shapeless|wire=packet106-accepted|oracle=three-remaining-dye-rest-damages+fresh-login|inputs=351:0+351:15+351:15+351:8+351:15+351:5+351:9,results=351x3:7+351x2:7+351x2:13,grid=2x2,actions=20,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`0334f546ce0368581cb95d0fcb41d97e63d257acb76e91c53b41c849cfac594d`.
