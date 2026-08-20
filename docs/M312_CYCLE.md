# M312 qualification cycle

`TorchInvertCycle` rebuilds the raised stone inverter in two fresh official
server JVMs. Each run places redstone torch item `76` on the north face as
live `76:4`, powers the support with a 1-tick repeater, freezes live `75:4`,
and reloads that inverted cell after save plus a fresh login. The signal
includes both `76` and `75` and is distinct from lit floor torch `76:5`. One
official EOF is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`e4b4e7bf13497288e3b90b76bd07f714f976ecc54254f40ae81e8150b4924ae9`.

Run directly with:

```text
java tools/smoke/TorchInvertCycle.java m312-torch-invert
```

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
