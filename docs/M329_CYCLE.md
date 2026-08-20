# M329 qualification cycle

`UtilityBlockCraftsCycle` rebuilds the workbench fixture in two fresh official
server JVMs. Each run crafts fence `85`, ladder `65`, and bookshelf `47`
from their vanilla recipes and reloads those stacks after save plus fresh
login. One official EOF is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`b70015b8e4bea597b4b8eeba287d216244d5c1bb9f83a1d7d06120bdb8c5086f`.

Run directly with:

```text
java tools/smoke/UtilityBlockCraftsCycle.java m329-utility-block-crafts
```

Canonical evidence uses two official server JVMs and four client sessions.
