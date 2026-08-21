# M273 qualification cycle

`ChainBootsCycle` rebuilds the chain-boots-305 fixture in two fresh official
server JVMs. Each run window-clicks item 305 into armor slot 8, proves peer
Packet5 slot 1 equals 305, and reloads that player state. One official EOF is
retried after a 5 second sleep.

The frozen semantic SHA-256 is
`509d729ffedcf64fb1478260c71654e80183c1936480c9db878459abe189ec16`.

Run directly with:

```text
java tools/smoke/ChainBootsCycle.java m273-chain-boots
```

Canonical evidence uses two official server JVMs and four client sessions.
