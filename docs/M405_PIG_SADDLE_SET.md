# M405 pig saddle set

M405 opens the official compound pig-plus-saddle boundary. Animals are
enabled so a default spawner `52` emits Packet24 type `90`. Packet7 button
0 while holding saddle item `329` consumes that stack. Empty-hand Packet7
button 0 then mounts the saddled pig; Packet39 attach is decoded on the
existing Packet23 object tracker.

This is distinct from shipping M149 pig death and M150 pork `319`. It does
not claim wheat breeding, pork drops, dismount, or persistence of the
saddle across restart. Headless `B173WireClient` only. No GUI. No Aero.

The frozen semantic SHA-256 is
`a27d2ce0c705f4fe5af56c8e35b8ec7c212956eaff46a764ce610d54f40c06d9`.
