<!-- worldline-map-schema=1 -->
<!-- boundary=b173-harvestable-vegetation-lifecycle-cycle -->
<!-- nonclaims=natural-growth,decay,random-seed-yield,hydration-transition,native-render -->
<!-- frozen-trace=0000000000000000000000000000000000000000000000000000000000000000 -->

# Beta 1.7.3 harvestable vegetation lifecycles

Four public TestKit rows execute complete isolated lifecycles for vegetation that is obtainable and
replaceable through historical gameplay. Player-placed leaves `18:8`, tall grass `31:1`, and dead
bush `32:0` are broken with shears `359`; age-zero crops `59:0` are planted with seeds `295` on
hydrated farmland `60:7` beside protocol-provisioned still water `9:0`.

Each row proves placement consumption, exact placed state, fresh-login persistence, break to air,
one exact drop, exact post-break tool state, and removed-state persistence after a second fresh
login. The crop row keeps its environmental water and hydrated farmland through both reloads.

The package joins sixteen lifecycle atoms into one vegetation mini-subsystem. Existing leaf and
crop break evidence means only twelve distinct census claims are expected to become newly verified.
It does not claim natural growth, leaf decay, mature crop yield, random seed chance, farmland
hydration transitions, biome tint, collision geometry, or native rendering.

Frozen signal:
`provider=b1.7.3-server-lifecycle,family=harvestable-vegetation,rows=4,passed=4,layers=U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A,reload=FRESH_LOGINx8,evidence=0000000000000000000000000000000000000000000000000000000000000000,isolation=4-fresh-worlds`.
