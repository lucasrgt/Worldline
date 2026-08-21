# M426 qualification cycle

`RemainingRedstoneFacesCycle` rebuilds the raised stone fixture in two
fresh official server JVMs. Each run places remaining lever `69` wall
faces `69:2`, `69:3`, and `69:4` plus two ground attachments, then places
remaining repeater facings `93:0`, `93:1`, and `93:2`. Empty-hand Packet15
tunes the east cell to delay 2 (`93:5`). A redstone torch `76:5` on the
input side powers that cell to `94:5`. A clean save plus fresh login keeps
the wall/ground levers, unpowered north/south diodes, and powered
`94:5`. Floor lever `5` versus `6` is vanilla `World.rand` and is hashed
only as ground attachment. One official EOF is retried after a 5 second
sleep. Headless `B173WireClient` is the only client. There is no GUI and
no Aero path.

A result matching M340's `69:1->9->1` latch, M341's west delay
`3->7->11->15`, or M399's button `77` faces fails.

Run directly with:

```text
java tools/smoke/RemainingRedstoneFacesCycle.java m426-remaining-redstone-faces
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`1bb55855bc7d7a3c3f9eef22fd7e235e02c3e5220a782fb29ed29a27bb69b44e`.
