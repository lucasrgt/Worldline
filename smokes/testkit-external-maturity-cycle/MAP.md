<!-- worldline-map-schema=1 -->
<!-- boundary=testkit-external-maturity -->
<!-- nonclaims=one-point-zero-promotion,loader-runtime-compatibility,filesystem-sandbox -->
<!-- frozen-trace=a038e9c1e2b75db11725b104b908fff44fd86ccd588d85ac399a81e0d30b7d4e -->

# External TestKit maturity map

`WorldlineContract.TESTKIT_EXTERNAL_MATURITY` binds `TestKitMaturityFixture#assess`.
Its consumers are the real Aero CPU differential suite and the real BetaEnergistics
terminal ordering and catalog differential suites. Every external spec stays unchanged.
Gradle resolves the public 0.3.0 plugin and checksum-pinned public API/runner JARs.
The qualifier rejects included builds and non-public Worldline compile/runner classpaths.
No official runtime is requested or started by the consumers (`noRuntime=true`).

The historical Beta proof is the public worldline-testkit job 97699745707, whose
five tests passed on 2026-08-25 at revision 80271a399963e013e0e2f92595194f9313fb4caa.
Aero current source targets unpublished 0.3.1; the test driver resolves public 0.3.0
without editing the consumer. Its older public branch cb5d790 has only general CI
success, not an executed Worldline job. Neither private history nor unrelated CI
is admitted as sustained public usage.

The explicit minimum is two successful observation dates spanning seven days per mod.
The baseline has two public consumers but only one sustained consumer; `requireReadyForOne`
therefore throws. A later public Aero observation can satisfy this gate; no time passage
or test execution is invented. This milestone qualifies the release gate and the actual
external baseline, while 1.0 publication remains blocked. Functional Census delta: zero.

Frozen signal: `external-mods=2,public-tests=9,sustained-mods=1,promotion-1.0=blocked,private-dependencies=0`.
