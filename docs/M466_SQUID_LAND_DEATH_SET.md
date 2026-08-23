# M466 squid land death set

M466 opens the official compound squid-on-land death boundary. A default
spawner `52` is placed in a dry room inside the squid spawn band
`45 < y < 63` and retargeted from `Pig` to `Squid`. Packet24 type `94`
must spawn out of water. Death is Packet38 status `3` plus Packet29
after Packet7 diamond-sword `276`. The claim is death while dry, not damage
caused by land exposure.

This is distinct from shipping M408, which kills type `94` in water and
claims ink sac `351:0`. It does not claim other water mobs, XP, or cooked
drops. Headless `B173WireClient` only. No GUI. No Aero.

The frozen semantic SHA-256 is
`3c3b628471c4ee01b5da67ea523767d75fcc305a6747025b28720ca05ecab8a6`.
