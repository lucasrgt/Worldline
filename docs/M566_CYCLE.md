# M566 qualification cycle

`GrassSpreadSetCycle` rebuilds the raised stone dirt/grass pad in two
fresh official server JVMs. Each run Packet15-places an 8-cell grass
`2` ring beside lit dirt `3` samples and one stone-covered dirt `3`,
then waits a bounded random-tick window until Packet53 `3->2` appears
on a lit sample while the covered cell stays dirt. One official EOF is
retried after a 5 second sleep.

The frozen signal must name `spread=3->2` and `covered-stay=true` and
must not claim M238/M223 place oracles.

Run directly with:

```text
java tools/smoke/GrassSpreadSetCycle.java m566-grass-spread-set
```

The frozen semantic SHA-256 is
`b80a81abefd273cf68a6495d0a825f6556e85176324d77bc1702832ffc448174`.

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
