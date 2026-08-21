# M180 fishing rod

M180 opens the official fishing-hook object boundary. Fishing rod item `346`
used through Packet15 direction 255 from a raised stone platform creates one
EntityFishHook. Two connected clients receive the identical Packet23 type-`90`
spawn with thrower `0` and quantized pose `138:2512:144`. Official b1.7.3
fishhooks use the two-argument Packet23 constructor, so thrower is `0` and
velocity is absent.

This milestone does not claim reel, catch item, bobber physics, fish-catch
RNG, or other Packet23 object types.
