# M310 qualification cycle

`VehicleRidesCycle` rebuilds the natural water cell and a nearby raised rail
in two fresh official server JVMs. Each run places boat item `333` and
minecart item `328`, empty-hand Packet7 mounts both vehicles, and freezes
Packet39 attach for type `1` and type `10`. Two peers still correlate both
Packet23 spawns. Headless `B173WireClient` is the only client. There is no
GUI and no Aero path. One official EOF is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`e9490bd2395a9a0e2f23738cb8956250a2a8738d5f0d1c62c27d254b43a8ff3f`.

Run directly with:

```text
java tools/smoke/VehicleRidesCycle.java m310-vehicle-rides
```

Canonical evidence uses two official server JVMs and four client sessions.
