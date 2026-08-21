# M403 remaining boat break

M403 opens the official compound vehicle-break set. Item `333` used through
Packet15 direction 255 while standing in still water `4:60:4:9:0` emits
protocol-14 Packet23 type `1`. Empty-hand Packet7 button `1` then attacks
that shared boat until Packet21 wreckage. The cycle places and breaks two
boats in one session so the family is not a single Packet15 place.

Official dedicated-server boat-break drops plank `5` and stick `280`. It
does not return boat item `333`. Both peers share each type-`1` identity
and the Packet21 wreckage.

This is distinct from shipping M154 (spawn only), shipping M326 (craft
only), and sibling M378 (spawn plus ride-then-detach). It does not claim
paddle control, minecart breaks, vehicle crafts, or ride persistence.
Headless `B173WireClient` only. No GUI. No Aero.

The frozen semantic SHA-256 is
`34eb6766ee9194e30d2efd5712a5e932110351176e336e526d7c6f23a877dedc`.
