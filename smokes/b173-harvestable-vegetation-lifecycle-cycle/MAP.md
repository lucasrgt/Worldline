<!-- worldline-map-schema=1 -->
<!-- boundary=b173-harvestable-vegetation-lifecycle-cycle -->
<!-- nonclaims=natural-growth,decay,tall-grass-harvest,crop-support-transition,native-render -->
<!-- frozen-trace=0000000000000000000000000000000000000000000000000000000000000000 -->

# Beta 1.7.3 shear-harvested leaf-variant lifecycles

Three public TestKit rows execute complete isolated lifecycles for oak, spruce, and birch leaves
obtained and harvested with historical shears. Carried variants `18:0`, `18:1`, and `18:2` pass
through the Beta ItemBlock placement path as states `18:8`, `18:9`, and `18:10` beside log `17:0`.
The log prevents unsupported-decay random ticks from contaminating the manual lifecycle boundary.
Shearing each placed state returns one matching original item variant. All rows use shears `359`.

Each row proves placement consumption, exact placed state, fresh-login persistence, break to air,
one exact drop, one point of shear durability, stable natural support, and removed-state persistence after
a second fresh login.

The package joins twelve lifecycle atoms into one metadata-sensitive leaf mini-subsystem. Existing
leaf break/drop evidence means two distinct census claims are expected to become newly verified.
It does not claim natural growth, leaf decay, tall-grass harvest, crop support transitions, biome tint, collision
geometry, or native rendering.

Frozen signal:
`provider=b1.7.3-server-lifecycle,family=shear-harvested-leaf-variants,rows=3,passed=3,layers=U-U-U-A+U-U-U-A+U-U-U-A,reload=FRESH_LOGINx6,evidence=0000000000000000000000000000000000000000000000000000000000000000,isolation=3-fresh-worlds`.
