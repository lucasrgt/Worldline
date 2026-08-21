# M333 qualification cycle

`DispenserSetCycle` rebuilds the raised west-facing dispenser fixture in two
fresh official server JVMs. Each run places dispenser `23`, loads cobblestone
`4` and oak planks `5` through the Trap window, and ejects both stacks with
side-lever pulses. One official EOF is retried after a 5 second sleep.

Run directly with:

```text
java tools/smoke/DispenserSetCycle.java m333-dispenser-set
```

Canonical evidence uses two official server JVMs and two client sessions.
The frozen semantic SHA-256 is
`46b62a083dad7f0e54a72e16e9b51144add22acb4cb53b75b51439b04385894e`.
