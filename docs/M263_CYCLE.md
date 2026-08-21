# M263 qualification cycle

`StewEatCycle` rebuilds the stew `282` fixture in two fresh official server
JVMs. Each run air-uses one mushroom stew, heals Packet8 `12 -> 20`, leaves
bowl `281` in the held slot, and reloads that inventory plus health.

The frozen semantic SHA-256 is
`94038e1a1f75ad42e97730c63d6089ab182511bd6f5889d8a1610d83e5471bc9`.

Canonical evidence uses two official server JVMs and four client sessions.
One official EOF is retried after a 5 second sleep.
