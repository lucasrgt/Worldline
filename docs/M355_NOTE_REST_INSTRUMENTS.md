# M355 remaining note-block instruments

M355 hashes the official note-block instrument-by-block-under rows that M313
left unhashed. Note block item `25` is placed with Packet15 onto glass `20`
and gold block `41`. Empty-hand Packet14 begin-dig then attacks each block.
The official dedicated server emits Packet54 play-note (block action), not
Packet61 world-event.

Left-click does not change `TileEntityNote` pitch. The first play on each
fresh note therefore sends Packet54 with pitch `0` and instrument `3` (glass)
and `0` (gold / default material). Clay would emit the same unused piano id.
Those Packet54 ids are distinct from M313's hashed `1`, `4`, and `2`. Block
`25` remains after a clean save plus fresh login.

Frozen semantic SHA-256:
`0b8bfa875138db6748a105c9ca98ad10bd8f4ff277dbe49e5d1d96e5790cf868`.

This milestone is distinct from M166, which right-clicks one stone-supported
note through Packet15 and tunes `0 -> 1`. It is distinct from M313, which
hashes stone, planks, and sand. It does not claim redstone-triggered notes,
a 25-click octave wrap, note particles, audio playback, or a second clay
cell as a separate hashed instrument. Headless `B173WireClient` only. No
GUI. No Aero.
