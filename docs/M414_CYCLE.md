# M414 qualification cycle

`LavaObsidianSetCycle` rebuilds the raised stone basins in two fresh
official server JVMs. Each run places lava bucket `327` and water bucket
`326` into a west adjacent pair, then into an isolated south adjacent
pair. Both lava-source cells must become obsidian `49`. The signal must
name `11`, water `9` or `8`, and two obsidian cells. One official EOF is
retried after a 5 second sleep. Headless `B173WireClient` is the only
client. There is no GUI and no Aero path.

Run directly with:

```text
java tools/smoke/LavaObsidianSetCycle.java m414-lava-obsidian-set
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`856101df96a1dea04d9f18e7a1ceef3018dce576227d046030271fa67825fbff`.
