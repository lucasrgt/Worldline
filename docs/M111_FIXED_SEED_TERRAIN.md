# M111 fixed-seed terrain

Status: GO in Worldline v1.99.0.

M111 is the first post-Aero milestone and returns the roadmap to vanilla
Minecraft. Two fresh unmodified official Beta 1.7.3 servers generate seed
`17320110707`; one protocol-14 client per server decodes absolute chunk `(0,0)`.

Both worlds contain exactly 16,342 non-air blocks in that chunk and share the
same 256-column top-Y/ID/metadata surface profile. The complete legacy block-ID
volume remains diagnostic: official population ordering can exchange a few
buried gravel and ore cells while preserving both the non-air count and surface.
The complete metadata plane is likewise diagnostic. The official server JAR is
hash-verified and never shipped by Worldline.

Beta 1.7.3 chooses a variable initial player spawn even under a fixed seed.
M111 therefore addresses the world by absolute chunk coordinate. M112
subsequently hardened the same unchanged oracle by writing a minimal official
player NBT at `(8.5,120,8.5)` before login. Spawn pose is printed only as
diagnostics and cannot affect the semantic signature.

This claim covers the exact chunk coordinate, block/non-air counts, and stated
surface profile. The complete block-ID and metadata planes, buried decoration
ordering, block light, sky light, biome identity, entity population, other
chunks, alternate seeds, Nether terrain, save/reload stability, and generic
world-generation equivalence remain open.
