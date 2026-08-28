<!-- worldline-map-schema=1 -->
<!-- boundary=b173-redstone-device-lifecycle-cycle -->
<!-- nonclaims=power-transition,orientation-domain,activation,neighbor-propagation,support-removal,native-render -->
<!-- frozen-trace=0000000000000000000000000000000000000000000000000000000000000000 -->

# Beta 1.7.3 redstone device lifecycles

Three public TestKit rows execute the complete neutral lifecycle of craftable components that
participate in redstone circuits. Dust item `331` places unpowered wire `55:0`; repeater item `356`
places the initial unpowered repeater state `93:2` at the provider's fixed player yaw; trapdoor
item `96` places a closed trapdoor `96:3` against the east face of stone.

Each isolated row proves the stone support, placement-item consumption, exact placed metadata,
fresh-login persistence, break to air, its historical item drop, unchanged stick, and removed-state
persistence after a second fresh login.

The package joins twelve lifecycle atoms into one redstone-device mini-subsystem. It does not claim
wire power, repeater power or delay transitions, full orientation domains, trapdoor activation,
neighbor propagation, support-removal reactions, collision geometry, or native rendering.

Frozen signal:
`provider=b1.7.3-server-lifecycle,family=redstone-devices,rows=3,passed=3,layers=U-U-U-A+U-U-U-A+U-U-U-A,reload=FRESH_LOGINx6,evidence=0000000000000000000000000000000000000000000000000000000000000000,isolation=3-fresh-worlds`.
