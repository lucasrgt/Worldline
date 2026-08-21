# M518-SW behavior map

The current RetroMCP mapping identifies official server class `kg` as
`EntityTNTPrimed`, field `a` as `fuse`, and inherited `m_()` as `onUpdate`.
The positional constructor initializes fuse `80`. Each entity update applies
the post-decrement boundary: samples are `79` after update 1, `40` after 40,
`1` after 79, `0` after 80, and `-1` plus dead/removal after update 81.

The Worldline controlled runtime and the official obfuscated JAR execute the
same positive, unprimed-block, and mid-fuse fixtures twice each. Constructor
random motion is zeroed before joining the world so the fuse claim is isolated.
The unprimed TNT block `46` remains a block with no entity; the mid-fuse
mutation stops at `40` with the entity live and no explosion.

Remote Packet23/Packet60 timing is not used as an internal-tick clock. This map
does not claim exact blast rays, chain priming, damage, persistence, or a new
explosion model.

Frozen semantic SHA-256:
`f6a9810837ac1a3622784617b05e6c957344be3be5493d4b261e359f18076f1d`.
