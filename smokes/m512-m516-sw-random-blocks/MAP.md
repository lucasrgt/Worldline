# M512/M516 random block mapping

The differential invokes `BlockGrass.updateTick` (official `oj.a`) and stationary-lava `BlockStationary.updateTick` (official `is.a`) with identical `Random` seeds. Grass spreads only into lit eligible dirt; roofed dirt and stone controls do not convert. Stationary lava ignites air adjacent to planks and wool, while the stone control never produces fire.

Frozen expected signature SHA-256: 60c5f2e1b9e9f3d743c2d19cfdefdf1829007df32559da47d52e0ba471ae5289

## Frozen semantic signal

`oracle=MATCH,fixture=m512-m516-sw-random-blocks,ticks=192,controlled=true`
