# M382 qualification cycle

`PortalObsidianSetCycle` rebuilds the raised stone column and `4x5`
obsidian frame in two fresh official server JVMs. Each run places
fourteen obsidian `49` cells, ignites the six interior cells with
flint-and-steel `259`, and reloads the frame plus portal `90:0` through a
fresh client. The player remains in dimension `0`. One official EOF is
retried after a 5 second sleep. Headless `B173WireClient` is the only
client. There is no GUI and no Aero path.

Run directly with:

```text
java tools/smoke/PortalObsidianSetCycle.java m382-portal-obsidian-set
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`6892e4aa2cd98f329d9e6c1b83cf4feed463e1ad996fe3afe61a0a36f8778f56`.
