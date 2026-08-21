# M441 remaining food rest set

M441 closes the official remaining ItemFood air-use rest family not covered
by M374. One official family seeds cookie `357` and mushroom stew `282`,
then eats each with Packet15 direction 255. Packet8 restores health
`19 -> 20` (cookie heal 1) and `12 -> 20` (stew heal 8). Packet103 consumes
cookie to empty and replaces stew with bowl `281`.

Beta 1.7.3 has no hunger bar; food heals health. Official food
`maxStackSize` is 1. Golden apple `322` is already hashed together in M374
and is not repeated. This is distinct from M374 remaining-food-eat, from
M327 food crafts, and from cake eat (M160 one-slice, M335 three-slice,
M369 cake eat). It does not claim bread, raw pork, fish, milk, BlockCake
`92`, or hunger-era food. Headless `B173WireClient` only. No GUI. No Aero.

The frozen semantic SHA-256 is
`a742d0481ec2e053071b64ffb13a565582bd3dbbc76859b4d650f2a8b74ac5b7`.
