# M433 remaining chest orient set

M433 opens the official remaining chest `54` facing and adjacent-pair
orientation set. Packet15 of chest item `54` on a raised stone row places
two isolated `54:0` cells from look yaw `-90` and `90`, then an east-west
adjacent pair and a north-south adjacent pair. The frozen signal names
those look yaws plus both pair axes. All six cells survive a clean save
plus fresh login.

This is distinct from shipping M232 (one isolated `54:0` with no look) and
from shipping M349 (one east-west pair plus Packet100 title `Large chest`
with 54 owned slots). It does not open Packet100, claim clicks, transfers,
or chest minecarts. Headless `B173WireClient` protocol 14 only. No GUI.
No Aero.

The frozen semantic SHA-256 is
`b9750e81a03028d1bb7345d6699d951772dea723fefb2cb303312f4c43423f03`.
