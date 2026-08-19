# M133 behavior map

One dimension-`0` client constructs and activates the exact M132 portal, moves
inside its active interior, and remains there for 120 official ticks. The
server emits Packet9 with dimension `-1`, followed by a corrected destination
pose and Nether chunk data.

The post-transition cache resolves the destination chunk `(-1,0)` with zero
skylight, stable netherrack/bedrock counts and the generated 14-obsidian,
6-portal counterpart. Exact portal coordinates vary legitimately between fresh
worlds and are deliberately excluded. The saved player remains in dimension
`-1`.

Frozen semantic SHA-256:
`5c8ac40f2065949243c4a0e77c0ae9f5757aa4d89247915f6878de01cb72ed5d`.
