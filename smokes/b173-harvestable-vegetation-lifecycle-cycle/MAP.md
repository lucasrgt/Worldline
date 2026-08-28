<!-- worldline-map-schema=1 -->
<!-- boundary=b173-harvestable-vegetation-lifecycle-cycle -->
<!-- nonclaims=natural-growth,decay,dead-bush-harvest,crop-support-transition,native-render -->
<!-- frozen-trace=0000000000000000000000000000000000000000000000000000000000000000 -->

# Beta 1.7.3 shear-harvested foliage lifecycles

Two public TestKit rows execute complete isolated lifecycles for foliage obtained and harvested
with historical shears. Player-placed leaves become `18:8`. A carried tall-grass variant `31:1`
passes through the Beta ItemBlock placement path as state `31:0`; shearing that placed state returns
one matching `31:0` item. Both rows use shears `359`.

Each row proves placement consumption, exact placed state, fresh-login persistence, break to air,
one exact drop, one point of shear durability, stable support, and removed-state persistence after
a second fresh login.

The package joins eight lifecycle atoms into one foliage mini-subsystem. Existing leaf break/drop
evidence means six distinct census claims are expected to become newly verified. It does not claim
natural growth, leaf decay, dead-bush harvest, crop support transitions, biome tint, collision
geometry, or native rendering.

Frozen signal:
`provider=b1.7.3-server-lifecycle,family=shear-harvested-foliage,rows=2,passed=2,layers=U-U-U-A+U-U-U-A,reload=FRESH_LOGINx4,evidence=0000000000000000000000000000000000000000000000000000000000000000,isolation=2-fresh-worlds`.
