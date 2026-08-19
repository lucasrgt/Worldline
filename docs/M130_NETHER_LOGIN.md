# M130 Nether login

Status: GO in Worldline v1.118.0.

M130 introduces an opt-in `allow-nether=true` dedicated-server profile and an
exact dimension-aware player seed. An official server logs the seeded player
into dimension `-1`, publishes a complete Nether chunk through protocol 14,
and persists `Dimension=-1` after clean logout.

Two fresh worlds reproduce the exact first-chunk structural census and
positional hash. Lava 10/11 and mushroom decorations 39/40 are excluded after
fresh-world diagnostics demonstrated scheduled-flow and decoration variation.

M130 does not claim portal construction or traversal, Packet9 respawn handling,
Overworld return, arbitrary Nether coordinates, entity populations, Nether
lighting, fluid state, decoration determinism, or a general dimension API.
