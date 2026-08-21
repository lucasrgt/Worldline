# M112 qualification cycle

`FixedSeedLightingCycle` verifies the official server artifact, compiles the
protocol-14 adapter and pre-login NBT seed with the M112 smoke, and runs two new server JVMs,
client sessions and worlds.

Each client targets absolute chunk `(0,0)`, validates the full-chunk shape,
hashes both light planes, builds exact sixteen-bin histograms, and disconnects
cleanly. The runner requires both rows, traces and signatures to match before
checking frozen evidence. Diagnostic mode cannot qualify.

Canonical evidence uses two official servers and two clients. Block-light
SHA-256 is `bc449b312209d70eb9ca4403aea691f640e3d4cccc1211e6dd10d073e393ad76`;
sky-light SHA-256 is
`ea9305667c4d0bbaf0e94c527d3aee028a42bd7dc5266bfdd744165e85f73663`.

The frozen semantic SHA-256 is
`f5180dc49e6d6117c501e903ab16b1015a071cedf027e2444168a40109dc0969`.
