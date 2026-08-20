# M344 qualification cycle

`BucketFluidSetCycle` rebuilds the raised stone basin in two fresh
official server JVMs. Each run places water bucket `326` into empty air,
picks the still water `9:0` back up, places lava bucket `327` into that
empty cell, picks the still lava `11:0` back up, and reloads the empty
basin plus both filled buckets. The signal must include place plus pickup
for `326/9` and `327/11`. One official EOF is retried after a 5 second
sleep. Headless `B173WireClient` is the only client. There is no GUI and
no Aero path.

Run directly with:

```text
java tools/smoke/BucketFluidSetCycle.java m344-bucket-fluid-set
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`fe76fdf6b8ec887d8efc4caa81ce926b3efad2a42207cbefd9b6a21f9b66b789`.
