# M290 qualification cycle

`BirchSaplingCycle` rebuilds the raised dirt-capped stone fixture in two
fresh official server JVMs. Each run places sapling item `6` (damage `2`)
on dirt `3` and reloads block `6:2`. One official EOF is retried after a
5 second sleep.

The frozen semantic SHA-256 is
`21f35395f38d2877297a2801023c0e7e0e0b5fc83a8ec278dee1ad7b7151b8a0`.

Run directly with:

```text
java tools/smoke/BirchSaplingCycle.java m290-birch-sapling
```

Canonical evidence uses two official server JVMs and four client sessions.
