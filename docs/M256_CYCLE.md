# M256 qualification cycle

`ChestMinecartCycle` rebuilds the raised rail fixture in two fresh official
server JVMs. Each run places rail `66` and chest-minecart item `342`, then
correlates Packet23 type `11` across two peers. One EOFException retries
after a 5s sleep.

The frozen semantic SHA-256 is
`77d7cc9f33cf75c87ba161f4e0b38376562e8c3a4a1bed0d9a78aaca8f9d0a74`.

Canonical evidence uses two official server JVMs and four client sessions.
