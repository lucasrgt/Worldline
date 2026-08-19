# M169 egg throw

M169 opens the official thrown-egg object boundary. Egg item `344` used through
Packet15 direction 255 from a raised stone platform creates one EntityEgg.
Two connected clients receive the identical Packet23 type-`62` spawn with
thrower `0` and quantized pose `138:2512:144`. Official b1.7.3 eggs use the
two-argument Packet23 constructor, so thrower is `0` and velocity is absent.

This milestone does not claim chicken hatch, impact damage, egg-item consume
count, snowballs, arrows, or other Packet23 throwable types.
