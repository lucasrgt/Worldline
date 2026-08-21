# M166 note block click

M166 opens the official note-block click boundary. Note-block item `25` is
placed with Packet15 onto a raised stone shelf as block `25:0`. Empty-hand
Packet15 then clicks the block. The official dedicated server emits Packet54
play-note (block action), not Packet61 world-event.

Pitch lives in `TileEntityNote`, not block metadata, so the cell stays `25:0`.
The first click increments that tile pitch `0 -> 1` and sends Packet54 with
instrument `1` (stone under the note) and pitch `1`. Block `25` remains after
a clean save plus fresh login.

This milestone does not claim the instrument-by-block-under table, a 25-click
octave wrap as a hashed sequence, note particles, or audio playback.
