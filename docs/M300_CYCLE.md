# M300 qualification cycle

`OrePickBreaksCycle` rebuilds the raised stone fixture in two fresh
official server JVMs. Each run places cobble item `4`, coal ore item
`16`, and diamond ore item `56`, then Packet14-digs cobble and coal with
iron pickaxe `257` and diamond ore with diamond pickaxe `278`. One
official EOF is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`5fa840f6542410b38170ae4dc2fe1d2731c8d7ca7335ba9d105d3c1feed61b1a`.

Canonical evidence uses two official server JVMs and two client sessions.
Headless protocol-14 only. No GUI. No Aero.

Run directly with:

```text
java tools/smoke/OrePickBreaksCycle.java m300-ore-pick-breaks
```
