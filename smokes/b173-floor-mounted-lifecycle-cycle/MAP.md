<!-- worldline-map-schema=1 -->
<!-- boundary=b173-floor-mounted-lifecycle-cycle -->
<!-- nonclaims=orientation-domain,redstone-activation,support-removal,light-propagation,native-render -->
<!-- frozen-trace=0000000000000000000000000000000000000000000000000000000000000000 -->

# Beta 1.7.3 floor-mounted block lifecycles

Six public TestKit rows exercise blocks whose gameplay lifecycle begins by placing them on top of
a solid floor. Wood and cobblestone stairs, pumpkin, jack-o'-lantern, torch, and lit redstone torch
each prove the exact default-yaw state, placement-item consumption, persistence across a fresh
login, break to air, exact item drop, break-tool state, and removed-state persistence after a
second fresh login.

Every row owns a fresh official world and uses the same raised stone fixture. Directional solid
blocks use their effective harvest tool, while the zero-hardness attachments use a non-damageable
stick so the tool-state oracle remains explicit. The package joins 24 lifecycle atoms into one
floor-mounted lifecycle mini-subsystem rather than counting one milestone per block or phase.

This map does not claim the complete orientation domains, redstone activation or burnout,
support-removal reactions, emitted-light propagation, collision geometry, or native rendering.
Those behaviors remain independently proved or outside this lifecycle boundary.

Frozen signal:
`provider=b1.7.3-server-lifecycle,family=floor-mounted,rows=6,passed=6,layers=U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A,reload=FRESH_LOGINx12,evidence=0000000000000000000000000000000000000000000000000000000000000000,isolation=6-fresh-worlds`.
