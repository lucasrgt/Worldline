# M124 behavior map

M124 recreates M123's exact edge glowstone and captures both lit chunks through
a fresh witness. Packet14 start/finish then removes the source; Packet53
confirms transient air and forty heartbeats allow ocean water to restore `9:0`.
A final fresh reader receives both complete Packet51 snapshots.

The 55 source-chunk and 19 neighboring-chunk block-light increases reverse as
exact decreases. Source and neighboring samples return from 15/12 to 0. Direct
baseline-to-final comparisons contain zero changed light samples in both chunks.

Frozen trace SHA-256:
`60903e4d40e5297e01412eb69996ce5f3e2b641f1898d67f376ff357d016dbce`.
