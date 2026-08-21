# M360 fishing catch set

M360 opens the official fishing-catch boundary on top of the M180 hook
spawn. Fishing rod item `346` used through Packet15 direction 255 from
the raised-stone dock creates one EntityFishHook. The same headless
session observes Packet23 type `90`, then reels an official Packet21
dropped raw fish `349`.

Frozen semantic SHA-256:
`b81e3dfcba437f67fee01101898bab64442120affa5b0cdb60dc16f69a2549b0`.

This milestone does not claim bobber physics hashing, junk loot, cooked
fish `350`, or other Packet23 object types. Headless `B173WireClient`
protocol-14 only. No GUI. No Aero.
