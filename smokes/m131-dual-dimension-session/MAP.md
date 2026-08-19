# M131 behavior map

One official server hosts two simultaneous clients seeded into dimensions `0`
and `-1`. Packet1's signed dimension byte is retained as typed session state.
Both clients decode chunk `(0,0)`; Overworld skylight is positive while Nether
skylight is zero and its structural terrain matches M130.

A production-path byte fixture feeds Packet9 `0→-1` to an inbound channel with
one decoded chunk and requires the old-dimension cache to fall from one to zero.

Frozen semantic SHA-256:
`4fbbe9be7e3cd6ab8fbfddd920b11392711702505cfd14044e93128570b457cd`.
