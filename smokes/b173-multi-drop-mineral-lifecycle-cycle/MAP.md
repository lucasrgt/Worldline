<!-- worldline-map-schema=1 -->
<!-- boundary=b173-multi-drop-mineral-lifecycle-cycle -->
<!-- nonclaims=fortune,silk-touch,experience,glowing-redstone-transition,crafting-compression,light-propagation,native-render -->
<!-- frozen-trace=0000000000000000000000000000000000000000000000000000000000000000 -->

# Beta 1.7.3 multi-drop mineral lifecycles

Three public TestKit rows execute complete isolated lifecycles for blocks whose historical harvest
converts one placed block into multiple item entities. Lapis ore `21:0` emits lapis dye `351:4`,
redstone ore `73:0` emits dust `331:0`, and glowstone `89:0` emits dust `348:0` when broken with
diamond pickaxe `278`.

Each row proves placement-item consumption, exact block state, fresh-login persistence, break to
air, every distinct dropped-item entity and its metadata, one point of pickaxe durability, and
removed-state persistence after a second fresh login.

The package joins twelve lifecycle atoms into one multi-drop mineral mini-subsystem. Existing
placement/reload evidence means six distinct census claims are expected to become newly verified.
It does not claim Fortune, silk touch, experience, glowing-redstone transitions, compressed-block
crafting, light propagation, collision geometry, or native rendering.

Frozen signal:
`provider=b1.7.3-server-lifecycle,family=multi-drop-minerals,rows=3,passed=3,layers=U-U-U-A+U-U-U-A+U-U-U-A,reload=FRESH_LOGINx6,evidence=0000000000000000000000000000000000000000000000000000000000000000,isolation=3-fresh-worlds`.
