# M369 cake full eat set

M369 continues the official compound cake-eat boundary past M335. Packet15
of cake item `354` on a raised stone support places uneaten cake `92:0`.
Empty-hand Packet15 then eats the remaining metadata slices beyond M335's
three: metadata advances `0 -> 1 -> 2 -> 3 -> 4 -> 5`, and the sixth bite
replaces the block with air `0`. Each bite requires `health < 20` and
restores three health points. Beta 1.7.3 has no hunger bar; cake heals
health.

The frozen signal includes multiple cake `92` metadata values past M335
(`3`, `4`, `5`) plus the sixth-bite air cell. That is distinct from M335
three-slice (`92:0->1->2->3`) and M160 one-slice eat (`92:0->1`). The air
cell remains after a clean save plus fresh login.

Frozen semantic SHA-256:
`1e7b764b96a4af45a053eec0e064137715747cb2554f80daaece626bee17a371`.

Official cake has six bites via metadata. This milestone proves the
remaining slices through air. It does not claim crafting, drops, or
hunger-era food. Headless `B173WireClient` only. No GUI. No Aero.
