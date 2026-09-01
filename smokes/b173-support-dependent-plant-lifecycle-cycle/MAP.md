<!-- worldline-map-schema=1 -->
<!-- boundary=b173-support-dependent-plant-lifecycle-cycle -->
<!-- nonclaims=random-tick-growth,neighbor-invalidation,collision-damage,native-render -->
<!-- frozen-trace=1eb01d29fa7d0e44a0e533f2c5ecd839bb5bc9534df1f05fb88adbe23d0bb1d5 -->

# Beta 1.7.3 support-dependent plant lifecycles

Two public TestKit rows execute complete isolated lifecycles for cactus and sugar cane. Cactus item
`81` is planted on gameplay-provisioned sand `12:0`. Sugar-cane item `338` is planted as block
`83:0` on dirt `3:0` beside still water `9:0`; that water is itself provisioned through the
official Packet15 block-placement path from a lab-seeded water item `9`, rather than direct world
mutation. The lab item is only environmental setup and is not reported as survival-obtainable.

Each row proves the substrate and optional lateral neighbor, placement consumption, exact placed
state, fresh-login persistence, break to air, one exact historical self-item drop, unchanged tool,
and removed-state persistence after a second fresh login. The water neighbor must remain `9:0`
through both reloads.

This map does not claim random-tick height growth, dry/adjacent-solid invalidation, cactus collision
damage, stacked-plant harvest propagation, recipes, particles, or native rendering. Those behavior
families retain their independent proofs.

Discovery signal:
`provider=b1.7.3-server-lifecycle,family=support-dependent-plants,rows=2,passed=2,layers=U-U-U-A+U-U-U-A,reload=FRESH_LOGINx4,evidence=8195254d1e2ba875de99423ec35b90958d583bdd97e4f20ed477b0c7b004831e,isolation=2-fresh-worlds`.
