# M25 Persisted Multiplayer Movement

Two fresh protocol-14 clients synchronize with two fresh official servers and
request a `+0.125 X` move. Spawn begins at the horizontal center of a block, so
this bounded displacement keeps the player footprint inside the same block
cell and does not depend on adjacent terrain.

After native packet handling, disconnect, and save, the gate requires exact
target coordinates in each official player file. Initial world-dependent
coordinates remain observational; the requested delta and accepted result are
frozen.
