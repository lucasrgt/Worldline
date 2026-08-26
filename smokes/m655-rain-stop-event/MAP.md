<!-- worldline-map-schema=1 -->
<!-- boundary=rain-stop-event -->
<!-- nonclaims=rendering,precipitation-effects,thunder,commands,minimum-duration -->
<!-- frozen-trace=85f90247a2a5cb89148caa85762e0582afa507d9ddba8bd92047938c28de8660 -->

# M655 rain stop event behavior map

The official Beta 1.7.3 dedicated server begins each replica from a normal generated world whose
`level.dat` weather fields are patched to `raining=true`, a bounded positive `rainTime`, and
`thundering=false`. Seed, world time, and spawn coordinates remain unchanged.

On login, `ServerConfigurationManager` emits Packet70 reason `1` for an already-raining world.
The adapter records that bootstrap before it permits the rain-stop observation to arm. The same
connection then observes the live `WorldServer.updateWeather` broadcast Packet70 reason `2` when
the countdown expires. `worldline.testkit.RainStopFixture#observe` normalizes that event as exact,
equatable `raining-before` and `dry-after` evidence.

M500 owns the complementary live dry-to-rain Packet70 reason `1` transition and explicitly does
not claim rain stop. M624 privately compares mapped and official weather state machinery but does
not publish a public behavior token or claim packet broadcasts. M655 therefore publishes the
distinct public `rain-stop-event` boundary without duplicating either identity.

This boundary does not claim client rain rendering, precipitation effects, thunder or lightning,
weather commands, a minimum rain duration, or persistence after restart.

Frozen signal:

```text
dimension=0,bootstrap=packet70-reason1,live=packet70-reason2,state=raining-before-dry-after,thundering=false,identity=seed-spawn-preserved,clients=1,disconnect=clean
```

Frozen trace SHA-256: `85f90247a2a5cb89148caa85762e0582afa507d9ddba8bd92047938c28de8660`.
