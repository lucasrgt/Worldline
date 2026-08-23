<!-- worldline-map-schema=1 -->
<!-- boundary=m135-player-respawn -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=22275e37f5b927fb38ddbe53bfb3869f752fa11afe00efc1e57d41edca84f81a -->

# M135 behavior map

One empty player is seeded at Y `-80` in each fresh official world. Bounded
heartbeats let vanilla void damage produce a signed nonpositive Packet8 health
value. The adapter then sends Packet9 with the current dimension byte and
requires a newer inbound Packet9 epoch plus Packet8 health `20`.

The post-respawn oracle follows the server correction rather than freezing a
spawn coordinate: the corrected chunk must decode, expose positive skylight,
retain dimension `0` and contain no player inventory. Clean save persists
health `20` and the empty inventory.

Frozen semantic SHA-256:
`22275e37f5b927fb38ddbe53bfb3869f752fa11afe00efc1e57d41edca84f81a`.
