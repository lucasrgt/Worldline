# M154 boat spawn

M154 opens the official vehicle/object spawn boundary. Item `333` used through
Packet15 direction 255 (official right-click raytrace) while standing in a
natural still-water cell `4:60:4:9:0` causes protocol-14 Packet23 type `1`.
Two headless peers observe the same entity identity and quantized pose
`144:1993:144`.

This milestone does not claim boat physics, paddle control, mount/dismount,
Packet39 attach, minecarts, arrows or other Packet23 object types.
