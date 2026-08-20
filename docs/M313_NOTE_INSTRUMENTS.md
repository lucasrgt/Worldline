# M313 note block instruments

M313 opens the official note-block instrument-by-block-under boundary. Note
block item `25` is placed with Packet15 onto three bases: raised stone, oak
planks `5`, and supported sand `12`. Empty-hand Packet14 begin-dig then
attacks each block. The official dedicated server emits Packet54 play-note
(block action), not Packet61 world-event.

Left-click does not change `TileEntityNote` pitch. The first play on each
fresh note therefore sends Packet54 with pitch `0` and instrument `1` (stone),
`4` (wood / planks), and `2` (sand). Block `25` remains after a clean save
plus fresh login.

This milestone is distinct from M166, which right-clicks one stone-supported
note through Packet15 and tunes `0 -> 1`. It is distinct from M328, which
plays one stone-supported note. It does not claim redstone-triggered notes,
a 25-click octave wrap, note particles, audio playback, or the remaining
glass and gold rows as a hashed table. Headless `B173WireClient` only. No
GUI. No Aero.
