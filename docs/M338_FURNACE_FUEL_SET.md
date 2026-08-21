# M338 furnace fuel set

M338 is the compound official furnace-fuel set. One cycle places three idle
furnaces `61:2` on a raised stone fixture and smelts cobble `4` to stone `1`
three times. The fuels are coal `263`, oak planks `5`, and lava bucket `327`.
Each load uses Packet100/102/103/105 evidence. Packet105 burn/progress is
`1600/1401`, `300/101`, and `20000/19801` respectively, with cook 199.

This milestone is distinct from M296 recipe outputs `265`, `266`, and `320`.
Charcoal is item `263` damage 1 and shares coal's 1600-tick burn; it is not a
separate Packet105 duration. Headless `B173WireClient` protocol-14 only. No
GUI. No Aero.

The frozen semantic SHA-256 is
`d412ed91eacea33e26daaf3f37c6494ecb462ee19694093f7126187f36a2b957`.
