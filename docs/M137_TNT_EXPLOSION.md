# M137 TNT explosion

Status: GO in Worldline v1.125.0.

M137 adds a typed explosion boundary to the cumulative multiplayer session.
The client builds an isolated stone column, places TNT block `46`, ignites it
with flint and steel `259`, and waits through the official fuse. Packet60 is
decoded into its center, strength and ordered relative destroyed-cell list;
those cells are also applied as air to the bounded remote cache.

Protocol 14 has no trailing player-motion floats in Packet60. The production
decoder deliberately models that Beta-specific layout rather than importing a
later protocol. The primed TNT entity moves randomly, so exact center and total
destroyed count remain dynamic. The oracle requires strength `4`, a nearby
center, a nonempty list containing the constructed support, live air for TNT
and support, and the same air state through a fresh client after save.

M137 does not claim exact blast rays, deterministic destroyed-cell count,
entity damage/knockback, chained TNT, fire, drops, resistance for arbitrary
blocks, cross-chunk blasts, sound/particle rendering or performance.
