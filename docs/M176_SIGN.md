# M176 sign text

M176 opens the official standing-sign text boundary. Packet15 of sign item
`323` on a raised stone support places standing sign `63:4` from actor look
yaw `-90`. Packet130 then writes four ASCII lines `World` / `line` / `M176`
/ `ok`. The official dedicated server accepts that tile, and a fresh login
reads the same four lines from inbound Packet130.

This milestone does not claim wall sign `68` as a second hashed type, color
codes, or a GUI editor.
