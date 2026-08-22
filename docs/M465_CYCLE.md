# M465 qualification cycle

`EnvDeathSetCycle` rebuilds the compound lava, drowning, and suffocation
death fixture in two fresh official server JVMs. Each run uses three
`writeInventory` health-20 logins so drowning, falling-sand head bury, and
still lava `11:0` each reach Packet8 health 0. One official EOF is retried
after a 5 second sleep.

The frozen signal must name lava `11`, drowning, sand `12` suffocation, and
`packet8=0`. It must not collapse to M307 hurt-only, M461 fall, or M469 void.

Run directly with:

```text
java tools/smoke/EnvDeathSetCycle.java m465-env-death-set
```

The frozen semantic SHA-256 is
`5f7c771e9c67210afff3c4f9afc8af6700507b0ce7d74239763d335b27bdf1b4`.

Canonical evidence uses two official server JVMs and six client sessions.
Headless `B173WireClient` protocol-14 only. No GUI. No Aero.
