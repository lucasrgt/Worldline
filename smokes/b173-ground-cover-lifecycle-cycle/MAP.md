<!-- worldline-map-schema=1 -->
<!-- boundary=b173-ground-cover-lifecycle-cycle -->
<!-- nonclaims=farmland-hoe-creation,crop-growth,bonemeal,worldgen-tall-grass,native-render,flora-physical-envelope -->
<!-- frozen-trace=dfbb3c9f3a5a4916dbb4ea35228ef40089e85003cf9287241276b8d4398601c9 -->

# Beta 1.7.3 ground-cover plant lifecycles

Three public TestKit rows execute complete isolated lifecycles for tall grass, dead bush, and
age-zero wheat. Tall-grass item `31` is planted on gameplay-provisioned dirt `3:0`. Dead-bush item
`32` is planted on sand `12:0`. Seeds item `295` is planted as crops `59:0` on protocol-provisioned
farmland `60:0`; that farmland is admin substrate setup and is not a hoe-tilling or farmland
gameplay-placement claim.

Each row proves the legal substrate, placement consumption, exact placed state, fresh-login
persistence, break to air, official drop identity and metadata within historical entity-count
bounds, an unchanged stick, and removed-state persistence after a second fresh login. Tall grass
may emit zero or one seed `295`. Dead bush may emit zero to two sticks `280`. Age-zero wheat may
emit zero to three seeds.

The package joins twelve Functional Census claims into one ground-cover mini-subsystem, including
requalifying the historical crop break and drop evidence onto this public TestKit signature. It
does not claim farmland creation with a hoe, crop growth or bonemeal, worldgen tall grass, ice or
glass, standing signs, native rendering, or flora physical envelopes.

Frozen signal:
`provider=b1.7.3-server-lifecycle,family=ground-cover,rows=3,passed=3,layers=U-U-U-A+U-U-U-A+U-U-U-A,reload=FRESH_LOGINx6,evidence=f42524a0b8629950eef218787a12338882c4c1025d7413bfc3a68156eba5932b,isolation=3-fresh-worlds`.

Frozen trace:
`v1|server=official-b1.7.3|seed=17320110707|provider=b1.7.3-server-lifecycle|family=ground-cover|rows=tall-grass+dead-bush+crops|actions=place+fresh-login+break+fresh-login|oracle=canonical-public-testkit-evidence|evidence=f42524a0b8629950eef218787a12338882c4c1025d7413bfc3a68156eba5932b`.
