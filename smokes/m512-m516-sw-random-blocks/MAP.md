# M512/M516 random block mapping

The differential invokes `BlockGrass.updateTick` (official `oj.a`) and stationary-lava `BlockStationary.updateTick` (official `is.a`) with identical `Random` seeds. Grass spreads only into lit eligible dirt; roofed dirt and stone controls do not convert. Stationary lava ignites air adjacent to planks and wool, while the stone control never produces fire.
