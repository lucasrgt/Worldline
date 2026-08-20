# M245 wall sign

M245 opens the official wall-sign text boundary. Packet15 of sign item
`323` against a raised stone east face places wall sign `68:5`. Packet130
then writes four UCS-2 lines `Wall` / `sign` / `M245` / `ok`, each at most
15 characters. The official dedicated server accepts that tile, and a
fresh login reads the same four lines from inbound Packet130.

This milestone does not claim standing sign `63`, color codes, or a GUI
editor. It reuses `RemoteSignText` and `B173SignAccess`.
