<!-- worldline-map-schema=1 -->
<!-- boundary=stationapi-testkit-runtime-driver -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=f7abd9d8fa0febff1a719e5a26cac9e1cb208539803ff8d025bb86466c3cb547 -->

# M620 StationAPI TestKit driver behavior map

M620 binds the neutral `TestRuntimeProvider` SPI to a second real runtime
family, `stationapi-b1.7.3`. Provider discovery is by the stable runtime ID,
not a CLI hard-code. Every TestKit attempt owns a new provider session, a new
official dedicated-server JVM, a new Fabric/StationAPI client JVM, a new
localhost control socket, and a caller-private world directory.

The injected client boundary waits for official login hello, player pose, and
remote chunk data. It then blocks the real client game thread at tick entry.
`TICK` releases exactly one client tick; the following entry returns a strict
state record before blocking again. The adapter exposes lifecycle state, world
time, player identity, health, selected slot, and finite pose. Every unqualified
write and broader domain operation fails closed.

The official client artifact resolved by the pinned StationAPI Gradle project
must have SHA-256
`af1fa04b8006d3ef78c7e24f8de4aa56f439a74d7f314827529062d5bab6db4c`.
The official server must have SHA-256
`033a127e4a25a60b038f15369c89305a3d53752242a1cff11ae964954e79ba4d`.
M620 does not claim singleplayer loading, block or inventory mutation, GUI
control, server-side StationAPI APIs, performance, or general StationAPI
equivalence.

Frozen signal:

```text
provider=stationapi-b1.7.3,discovery=spi,sessions=2,testkit=2-pass,ticks=2,isolation=fresh-client+server
```

Frozen trace:

```text
v1|provider=stationapi-b1.7.3|discovery=java-spi|sessions=2-fresh|processes=official-server+fabric-stationapi-client-per-test|readiness=login+pose+chunk|control=one-command-one-tick|surface=lifecycle+time+identity+health+slot+pose|shutdown=clean|broader-behavior=not-claimed
```

Frozen semantic SHA-256:
`f7abd9d8fa0febff1a719e5a26cac9e1cb208539803ff8d025bb86466c3cb547`.
