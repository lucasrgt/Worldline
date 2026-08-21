# M408 squid ink set

M408 opens the official compound squid water-habitat plus ink-sac drop
boundary. `B173DedicatedServer.animals` loads seed water in chunk `0,0`.
The headless protocol-14 client docks at the water surface, waits for
Packet24 type `94`, kills that identity with Packet7 diamond sword `276`,
and observes Packet21 ink sac `351:0`.

This is distinct from M328, which uses ink sac `351:0` only as a 2x2 craft
input, and from M389, which freezes cow `92` leather `334` plus chicken `93`
feather `288` on a grass platform. Headless `B173WireClient` only. No GUI.
No Aero.

The frozen semantic SHA-256 is
`4f3c68e6439036720158970ea6fb62f2db5d9bb980f42850dbb0cfdf53ac0f41`.
