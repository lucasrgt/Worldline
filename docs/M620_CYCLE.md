# M620-STATIONAPI-TESTKIT-DRIVER StationAPI TestKit driver

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M620 adds StationAPI as the second real TestKit runtime family. The neutral runner discovers `stationapi-b1.7.3` through Java SPI, and each test receives a fresh official Beta 1.7.3 server plus a fresh Fabric/StationAPI client. A localhost control channel gates the real client game thread one tick at a time and exposes only the qualified read-only state: lifecycle, world time, player identity, health, selected slot, and pose. The Worldline-owned runtime overlay also seals one capability-exact profiler artifact per client. Block mutation, teleport, inventory mutation, GUI control, and broader StationAPI behavior remain fail-closed and unclaimed.

## Qualification cycle

`M620StationapiTestkitDriverCycle` verifies the pinned client and server hashes and the pinned clean Aero checkout that owns the bare StationAPI Gradle project. It compiles the external adapter, publishes its `TestRuntimeProvider` service descriptor, compiles one Java 8 TestKit spec, and runs two cases. Each case opens and closes distinct client/server process trees, observes official login and chunk readiness, advances exactly one gated client tick, validates finite player state, and seals a nonempty WLPR capture. The complete isolated qualification is `java tools/harness/Gate.java --milestone m620-stationapi-testkit-driver`.

Expected signal: `provider=stationapi-b1.7.3,discovery=spi,sessions=2,testkit=2-pass,ticks=2,isolation=fresh-client+server,profiler=2-sealed-wlpr`.

Frozen semantic SHA-256: `482edeab1cf5f710c35a8abeda74e53d4cefc541d848fa6d9318a5500d275956`.
