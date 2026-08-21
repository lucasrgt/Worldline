# M385 leaf decay set

M385 opens the official compound leaf-decay boundary. Packet15 places oak
leaves item `18:0`, spruce leaves item `18:1`, and birch leaves item `18:2`
beside matching logs `17:0`, `17:1`, and `17:2`. Packet14 removes each log.
A bounded random-tick wait then proves all three cells decay to air
`0:0`. The same air cells survive a clean save and a fresh login.

Placed identity is `18:8`, `18:9`, and `18:10` (species plus the
decay-check bit). This milestone is distinct from M209, M291, and M292,
which keep nearby wood and freeze place-only persistence. It does not
claim shear drops, sapling drop rates, player-placed persistence without
wood, or a Worldline decay simulator.

Frozen semantic SHA-256:
`3974fe1e9ab8e39e20e8122dce05d183745ba923b5b1dd4306f63c308e0f2e1c`.

Headless `B173WireClient` only. No GUI. No Aero.
