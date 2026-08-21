# M439 qualification cycle

`RemainingOrePlaceSetCycle` rebuilds the raised stone pads in two fresh
official server JVMs. Each run Packet15-places coal ore `16`, lapis ore
`21`, and redstone ore `73`, then reloads those cells after save plus
fresh login. The frozen signal must name `16`, `21`, and `73`. The trace
must remain a Packet15 ore-place family and must not collapse into
Packet14 pick harvest. One official EOF is retried after a 5 second
sleep.

Run directly with:

```text
java tools/smoke/RemainingOrePlaceSetCycle.java m439-remaining-ore-place-set
```

The frozen semantic SHA-256 is
`0c58ca403f7064fde875a5257d07193fe9916277c21455b47ac366ab28b828ab`.

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
