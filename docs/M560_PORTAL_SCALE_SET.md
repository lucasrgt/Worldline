# M560 portal-scale set

M560 freezes the official Beta 1.7.3 Overworld-to-Nether 8:1 portal
coordinate scale. A known far Overworld portal at chunk `(20,20)` is
built and ignited, then entered for the vanilla residence interval.
Packet9 changes the live session to dimension `-1`. The corrected
Packet13 pose, quantized to block coordinates, lies within 128 blocks
of `(x/8, z/8)` and is farther than 128 blocks from the Overworld
source, so the mapping is 8:1 rather than 1:1 or "nether exists".

The frozen source column is `325,66,331`; the quantized 8:1 expectation
is `40,41`. This is distinct from M132 portal activation, M133 traversal
that only proves a Nether destination near spawn, M134 roundtrip travel,
and M382 frame-ignite without Packet9. Headless `B173WireClient` only.
No GUI. No Aero. `allow-nether` is true.

The frozen semantic SHA-256 is
`d7eb052e1bc5fe6a71f3850bd4fb75b9470be6a2767c6617fb41f7138c54c50b`.
