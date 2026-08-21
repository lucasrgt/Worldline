# M339 qualification cycle

`SaplingGrowthSetCycle` rebuilds the raised dirt sapling pads in two
fresh official server JVMs. Each run plants oak `6:0`, spruce `6:1`, and
birch `6:2`, fertilizes with bonemeal `351:15`, and requires matching log
roots `17:0`, `17:1`, and `17:2` after save plus fresh login. One official
EOF is retried after a 5 second sleep. Headless `B173WireClient`
protocol-14 is the only client. There is no GUI and no Aero path.

The frozen semantic SHA-256 is
`cbb09ab44fa0804f8304e414f683a868c16aabac0c29c00ba78b525e6678ec5e`.

Run directly with:

```text
java tools/smoke/SaplingGrowthSetCycle.java m339-sapling-growth-set
```

Canonical evidence uses two official server JVMs and four client sessions.
