# M130 qualification cycle

`NetherLoginCycle` verifies the official server artifact, compiles the updated
server adapter, and repeats dimension-seeded login, chunk decoding, logout and
saved-player inspection in two fresh workspaces.

Both runs must reproduce the exact dimension, pose, structural census,
normalized terrain hash, trace and frozen SHA-256:
`d04ef062cdda13bb2209d8f6651f0559495d9a9f63f946f460b0e8610c41c4a8`.

Canonical evidence uses two official server JVMs and two client sessions.
