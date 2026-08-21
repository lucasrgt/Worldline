# M112 fixed-seed lighting

Status: GO in Worldline v1.100.0.

M112 qualifies both complete vanilla light planes accompanying M111's exact
terrain. Two fresh unmodified official Beta 1.7.3 servers generate seed
`17320110707`; protocol-14 clients decode all 32,768 block-light and sky-light
nibbles in absolute chunk `(0,0)`.

M112 writes a minimal official-format player NBT at `(8.5,120,8.5)` after
server boot and before login. The official server loads that persisted pose
and generates the target normally, making absolute observation independent of
the version's variable spawn search without editing any world block.

Both runs produce identical plane hashes and exact 0–15 histograms. Block
light contains 32,702 zeros and 66 samples at levels 12–15. Sky light contains
15,360 zeros, 16,384 level-15 samples, and 256 samples at each of levels
3, 6, 9 and 12.

This is a deterministic snapshot boundary, not yet a light-engine transition.
M112 does not identify the emitting blocks, update or remove a source, freeze
global brightness, prove neighbor propagation, rendering, persistence,
alternate terrain, Nether lighting, or generic illumination semantics.
