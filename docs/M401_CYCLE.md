# M401 qualification cycle

`RemainingRedstoneWireCycle` rebuilds the raised stone pads in two fresh
official server JVMs. Each run places redstone dust `331` as a four-arm
cross, an east-west line, and a south-east elbow of wire `55:0`, then
reloads those cells after save plus fresh login. The signal must name
`cross`, `line`, and `elbow` with distinct connection masks. A single-cell
result matching M243, a powered `55:15` matching M116/M126, or rail bits
from M309 fails. One official EOF is retried after a 5 second sleep.

Run directly with:

```text
java tools/smoke/RemainingRedstoneWireCycle.java m401-remaining-redstone-wire
```

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero. The frozen semantic SHA-256 is
`b37e39c18b5b7ba396453c42ce9a726e1b0b51ab26949df34031ab9c9ddcd82e`.
