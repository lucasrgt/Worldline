# M303 qualification cycle

`CropHarvestsCycle` rebuilds the raised wheat, reed, and cactus fixture in
two fresh official server JVMs. Each run plants seeds `295` as wheat `59:0`,
bone-meals that crop to `59:7`, plants cane `83` and cactus `81`, then
Packet14-breaks all three cells. Headless `B173WireClient` is the only
client. There is no GUI and no Aero path. One official EOF is retried after
a 5 second sleep.

The frozen semantic SHA-256 is
`33bca9f328ddb3c028b792f70233157d997e260e28d47d0115069be6bcba67f0`.

Run directly with:

```text
java tools/smoke/CropHarvestsCycle.java m303-crop-harvests
```

Canonical evidence uses two official server JVMs and two client sessions.
