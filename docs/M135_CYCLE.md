# M135 qualification cycle

`PlayerRespawnCycle` repeats the void-death and Packet9 respawn path in two
fresh official server JVMs. A production-path byte fixture freezes requests
for dimension `0` and `-1` as `09-00|09-ff`.

Both runs reproduce nonpositive death health, a fresh same-dimension Packet9
epoch, Packet8 health `20`, a loaded Overworld chunk with positive skylight,
an empty inventory, clean disconnect and persisted health `20`. Their frozen
semantic SHA-256 is
`22275e37f5b927fb38ddbe53bfb3869f752fa11afe00efc1e57d41edca84f81a`.

Canonical evidence uses two official server JVMs and two client sessions.
