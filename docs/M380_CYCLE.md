# M380 qualification cycle

`TrapdoorFamilySetCycle` rebuilds the raised stone plus four-face trapdoor
fixture in two fresh official server JVMs. Each run places item `96` on
south, north, east, and west, toggles each cell open then closed, and
reloads the four closed cells. The signal must include `96:1->5->1`,
`96:0->4->0`, `96:3->7->3`, and `96:2->6->2`. One official EOF is retried
after a 5 second sleep. Headless `B173WireClient` is the only client.
There is no GUI and no Aero path.

Run directly with:

```text
java tools/smoke/TrapdoorFamilySetCycle.java m380-trapdoor-family-set
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`ab78b72d72f7fa3016aff5ef1e7d1fa6d51961bb14c02d74afa5e1a5ecf036e7`.
