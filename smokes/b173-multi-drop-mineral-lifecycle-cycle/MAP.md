<!-- worldline-map-schema=1 -->
<!-- boundary=b173-multi-drop-mineral-lifecycle-cycle -->
<!-- nonclaims=fortune,silk-touch,experience,glowing-redstone-transition,crafting-compression,light-propagation,native-render -->
<!-- frozen-trace=6436e4abec07a96a37d602dc29b1bcf5c0d8c3d46f3b6f7fa9b979bdd46abb17 -->

# Beta 1.7.3 multi-drop mineral lifecycles

Three public TestKit rows execute complete isolated lifecycles for blocks whose historical harvest
converts one placed block into multiple item entities. Lapis ore `21:0` emits lapis dye `351:4`,
redstone ore `73:0` emits dust `331:0`, and glowstone `89:0` emits dust `348:0` when broken with
diamond pickaxe `278`.

Each row proves placement-item consumption, exact block state, fresh-login persistence, break to
air, every dropped entity's exact item and metadata within the historical bounds (lapis 4..8,
redstone 4..5, glowstone 2..4), one point of pickaxe durability, and removed-state persistence
after a second fresh login.

The package joins twelve lifecycle atoms into one multi-drop mineral mini-subsystem. Existing
placement/reload evidence means six distinct census claims are expected to become newly verified.
It does not claim Fortune, silk touch, experience, glowing-redstone transitions, compressed-block
crafting, light propagation, collision geometry, or native rendering.

Frozen signal:
`provider=b1.7.3-server-lifecycle,family=multi-drop-minerals,rows=3,passed=3,layers=U-U-U-A+U-U-U-A+U-U-U-A,reload=FRESH_LOGINx6,evidence=3b1fe38f8dfadb67a808a510a018ac94ef3a25d743375923f68cafa59292e82a,isolation=3-fresh-worlds`.
