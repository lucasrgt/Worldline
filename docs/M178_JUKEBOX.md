# M178 jukebox

M178 opens the official jukebox insert boundary. Jukebox item `84` is placed
on a raised stone shelf as block `84:0`. Gold disc `2256` is selected and used
on that cell. The official dedicated server emits Packet61 effect `1005` data
`2256`, empties the selected slot through Packet103, and writes metadata
`84:1`. That exact cell remains after a clean save plus fresh login.

This milestone does not claim audio playback, eject-on-break hashing, or all
11 discs.
