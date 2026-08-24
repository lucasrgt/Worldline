# M625-MULTIPLAYER-EDGE-SET Multiplayer edge set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

This milestone freezes bounded multiplayer ordering, isolated disconnect, same-user reconnect, and pose persistence across server restart.

## Qualification cycle

M625 runs two official dedicated-server boots. Alpha then Beta connect on the first boot; Alpha moves, disconnects while Beta remains, and reconnects with a fresh entity identity at the persisted pose. After a clean save and restart, Beta then Alpha connect in reverse order and Alpha must resume the same persisted pose. Two fresh official cycles must emit the same evidence.

Expected signal: `orders=Alpha>Beta+Beta>Alpha,disconnect=isolated,reconnect=same-user-new-entity,persistence=disconnect+restart,clients=5,servers=2`.

Frozen semantic SHA-256: `d63e6e3d624950298e04542285ce3e9639d5d15be3b3d0866b35a9549f98e878`.
