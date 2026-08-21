# M427 qualification cycle

`RemainingPistonOrientSetCycle` rebuilds the raised stone fixture in two
fresh official server JVMs. Each run Packet15-places piston `33` and
sticky piston `29` with remaining look-derived facings `0`, `2`, `3`,
`4`, and `5`, then reloads those ten cells after save plus fresh login.
The frozen signal must name both `33` and `29` plus remaining metas and
must not claim up facing `:1` or M367 motion. One official EOF is
retried after a 5 second sleep.

Run directly with:

```text
java tools/smoke/RemainingPistonOrientSetCycle.java m427-remaining-piston-orient-set
```

The frozen semantic SHA-256 is
`467d62056ad74b5561c6e6bf67533b1608d7fc66644062154b00b81109e8ad76`.

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
