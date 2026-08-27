<!-- worldline-map-schema=1 -->
<!-- boundary=b173-support-face-attachment-lifecycle-cycle -->
<!-- nonclaims=orientation-domain,activation,support-removal,sign-text,native-render -->
<!-- frozen-trace=38ad1c32bb6b8b23c58b0027e022835979e4e8e67d27ccaed19c1f6a9ac03703 -->

# Beta 1.7.3 support-face attachment lifecycles

Four public TestKit rows exercise components whose gameplay lifecycle begins by attaching them to
the side of a solid support. Ladder `65:5`, wall sign `68:5`, lever `69:1`, and stone button `77:1`
are each placed on the east face of stone through official protocol-14 gameplay actions.

Each isolated row proves its stone support, placement-item consumption, exact placed metadata,
fresh-login persistence, break to air, exact historical drop, unchanged stick, and removed-state
persistence after a second fresh login. The wall-sign row additionally proves the historical
item `323` to block `68` placement and block `68` to item `323` drop boundary.

The package joins sixteen lifecycle atoms into one support-face attachment mini-subsystem. It does
not claim all orientation metadata, lever/button activation, reaction to removing the support,
sign text editing, redstone propagation, collision geometry, or native rendering.

Frozen signal:
`provider=b1.7.3-server-lifecycle,family=support-face-attachments,rows=4,passed=4,layers=U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A,reload=FRESH_LOGINx8,evidence=5c9fcc6327e454cf3ed031723e5b4908f6a3a0df53cfb4b6d1afaf889994608d,isolation=4-fresh-worlds`.
