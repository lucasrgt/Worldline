# M416 qualification cycle

`RemainingBookshelfPlaceCycle` rebuilds the workbench fixture in two fresh
official server JVMs. Each run crafts bookshelf `47` from six planks `5`
plus three books `340`, places two `47` cells, harvests one to air with
Packet21 `340` absent, then reloads the remaining cell after save plus
fresh login. The signal must name craft `47`, multiple places, and drop
`340`. One official EOF is retried after a 5 second sleep. Headless
`B173WireClient` is the only client. There is no GUI and no Aero path.

Run directly with:

```text
java tools/smoke/RemainingBookshelfPlaceCycle.java m416-remaining-bookshelf-place
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`63f78903270a88d3a7b5dafcb9aae55b9ffdacf7d785ff4b0f3d7616a975cc64`.
