# M111 fixed-seed terrain

Status: GO in Worldline v1.99.0.

M111 is the first post-Aero milestone and returns the roadmap to vanilla
Minecraft. Two fresh unmodified official Beta 1.7.3 servers generate seed
`17320110707`; one protocol-14 client per server decodes absolute chunk `(0,0)`.

Both worlds contain exactly 13,992 solid cells in that chunk, where solid
excludes air, water, and lava. Their complete solid mask and their 256-column
top-Y/ID/metadata surface profile are byte-for-byte identical. Raw block IDs
remain diagnostic: the official generator can resolve overlapping underground
decorators differently while preserving the promoted geometry. The complete
metadata plane is also diagnostic. The official server JAR is hash-verified
and never shipped by Worldline.

Beta 1.7.3 chooses a variable initial player spawn even under a fixed seed.
M111 therefore addresses the world by absolute chunk coordinate. M112
subsequently hardened the same unchanged oracle by writing a minimal official
player NBT at `(8.5,120,8.5)` before login. Spawn pose is printed only as
diagnostics and cannot affect the semantic signature.

This claim covers solid terrain occupancy and the stated surface profile only.
Raw interior block identity, non-air count, the complete metadata plane, block light, sky light, biome
identity, entity population, other chunks, alternate seeds, Nether terrain,
save/reload stability, and generic world-generation equivalence remain open.
