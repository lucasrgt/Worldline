# M289 qualification cycle

`SpruceSaplingCycle` rebuilds the raised dirt-capped stone fixture in two
fresh official server JVMs. Each run places sapling item `6` (damage `1`)
on dirt `3`, freezes live `6:1`, and reloads that cell after save plus a
fresh login. One official EOF is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`338c07cf0cc26fad4d048f900242741d71662eac8c8f48d98d41ede8c541dc2c`.

Run directly with:

```text
java tools/smoke/SpruceSaplingCycle.java m289-spruce-sapling
```

Canonical evidence uses two official server JVMs and four client sessions.
