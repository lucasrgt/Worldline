# M152 fire wool consumption

M152 composes the M151 netherrack flame and opens official fire consumption.
Flint and steel Packet15 on netherrack `87` places fire `51` above it. Wool
`35` is placed in a cell `BlockFire.tryToCatchBlockOnFire` can consume
(face-adjacent; wool `abilityToCatchFire=60`). After at most 1200 heartbeat
ticks, a fresh login proves the wool cell is air `0` or fire `51` while the
netherrack fire remains.

The frozen signal is categorical. It does not record the random consume delay
or whether the wool cell became air versus fire.

This milestone does not claim leaf/wood spread, rain extinguishing, fire
charge, or Nether-dimension ignition.
