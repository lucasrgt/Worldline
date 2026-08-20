# M375 qualification cycle

`RemainingPickBreaksCycle` rebuilds the raised stone fixture in two
fresh official server JVMs. Each run places mossy cobble item `48`, gold
ore item `14`, and obsidian item `49`, then Packet14-digs mossy cobble
with gold pickaxe `285` and gold ore plus obsidian with diamond pickaxe
`278`. The signal must include multiple remaining block ids `48`, `14`,
and `49`. One official EOF is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`22503c04e191d5edd6c2374799f5062269ff1e38d71c15709e468a2d2e787869`.

Canonical evidence uses two official server JVMs and two client sessions.
Headless protocol-14 only. No GUI. No Aero.

Run directly with:

```text
java tools/smoke/RemainingPickBreaksCycle.java m375-remaining-pick-breaks
```
