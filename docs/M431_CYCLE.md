# M431 qualification cycle

`RemainingBedOrientSetCycle` rebuilds a raised Overworld stone platform in
two fresh official server JVMs. Each run Packet15-places item `355` with
look yaw `90`, `180`, and `-90`, then reloads west `26:1`/`26:9`, north
`26:2`/`26:10`, and east `26:3`/`26:11` after save plus fresh login. The
frozen signal must name dimension `0` and remaining foot/head facings. It
must not claim Packet17 sleep, occupied `26:12`, or Nether Packet60. One
official EOF is retried after a 5 second sleep.

Run directly with:

```text
java tools/smoke/RemainingBedOrientSetCycle.java m431-remaining-bed-orient-set
```

The frozen semantic SHA-256 is
`8aa709e05da8be4a281e9eded3c6297e0f4236a515d73d60178570c69cf303a1`.

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
