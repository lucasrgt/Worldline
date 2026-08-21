# M374 remaining food eat

M374 opens the official remaining ItemFood air-use set. One official
family seeds apple `260`, cooked pork `320`, and golden apple `322`, then
eats each with Packet15 direction 255. Packet8 restores health
`16 -> 20` (apple heal 4), `12 -> 20` (cooked pork heal 8), and `10 -> 20`
(golden apple heal 20, cap 20). Packet103 consumes each stack to empty.

Beta 1.7.3 has no hunger bar; food heals health. Official food
`maxStackSize` is 1. This is distinct from M327 food crafts and from cake
eat (M160 one-slice, M335 three-slice, M369 cake eat). It does not claim
bread, cookie, stew, raw pork, fish, milk, BlockCake `92`, or hunger-era
food. Headless `B173WireClient` only. No GUI. No Aero.

The frozen semantic SHA-256 is
`8039053be1dc2477fd129e75dd6f6facd47634f0d8dc9e0be131b9750c9e2215`.
