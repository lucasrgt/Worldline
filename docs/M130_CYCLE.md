# M130 qualification cycle

`NetherLoginCycle` verifies the official server artifact, compiles the updated
server adapter, and repeats dimension-seeded login, chunk decoding, logout and
saved-player inspection in two fresh workspaces.

Both runs must reproduce the exact dimension, pose, structural census,
normalized terrain hash, trace and frozen SHA-256:
`ec56849776288464b6b19f00d5e977802847f155bcd1d8139a3816c7c53b7824`.

Canonical evidence uses two official server JVMs and two client sessions.
