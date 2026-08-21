# M129 qualification cycle

`CrossChunkIronDoorRecoveryCycle` repeats construction, opening, fresh-client
precondition, closing and fresh-reader recovery in two new official worlds.

Both runs must reproduce the inverse `2 + 1` state partition, empty residuals,
trace and frozen SHA-256:
`5a5478fd4aea68c69ed892984bc98353e208065f354857a047bac7f38c00cfac`.

Canonical evidence uses two official server JVMs and six client sessions.
