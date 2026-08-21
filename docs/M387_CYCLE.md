# M387 qualification cycle

`RemainingLightSetCycle` rebuilds the raised stone fixture in two fresh
official server JVMs. Each run places glowstone item `89`, jack-o-lantern
item `91` with look yaw `-90`, and torch item `50` on adjacent pads, then
reloads glowstone `89:0`, jack-o-lantern `91:1`, and floor torch `50:5`.
The signal must include `89+91+50`. One official EOF is retried after a 5
second sleep. Headless `B173WireClient` is the only client. There is no
GUI and no Aero path.

Run directly with:

```text
java tools/smoke/RemainingLightSetCycle.java m387-remaining-light-set
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`c8fb22dfee19b993ff3351bf0dfcb8de29c0975c84ee50c94848cd2d0e4c6d70`.
