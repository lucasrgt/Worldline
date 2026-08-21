# M378 boat water set

M378 opens the official compound boat-on-water set. Item `333` used through
Packet15 direction 255 while standing in still water `4:60:4:9:0` emits
protocol-14 Packet23 type `1`. Empty-hand Packet7 mounts the shared boat
and Packet39 attach is decoded on that same object tracker. A second
empty-hand Packet7 toggles the official unmount and Packet39 vehicle `-1`
is the detach.

This is distinct from shipping M154 (spawn only) and from shipping M326
(craft only). It does not claim paddle control, minecart rides, vehicle
crafts, or persistence of the ride across restart. Headless
`B173WireClient` only. No GUI. No Aero.

The frozen semantic SHA-256 is
`bdd585b5e79c816f4761039c63a02aa8e9f6164e77d7baa4fa4b3980a6a8d905`.
