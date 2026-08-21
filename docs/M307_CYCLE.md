# M307 qualification cycle

`EnvDamageCycle` rebuilds the compound drowning, suffocation, and lava
fixture in two fresh official server JVMs. Each run submerges until drowning
drops health `20 -> 18` with Packet38 status 2, takes one falling-sand
suffocation hit `19 -> 18`, then stands in still lava `11:0` for `19 -> 15`.
One official EOF is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`8a51289b35f57567a0dfbc0f3cf8f1d6981dac6219b52d494aac34f56713cba7`.

Canonical evidence uses two official server JVMs and four client sessions.
Headless `B173WireClient` only; no GUI and no Aero.

```text
java tools/smoke/EnvDamageCycle.java m307-env-damage
```
