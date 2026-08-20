# M434 qualification cycle

`RemainingSpongeGlassIceCycle` rebuilds the raised stone fixture in two
fresh official server JVMs. Each run Packet15-places sponge item `19`,
glass item `20`, and ice item `79` on adjacent pads, then reloads sponge
`19:0`, glass `20:0`, and ice `79:0`. Ice stays ice. The signal must
include `19+20+79` and must not name torch `50`, snow `78`, or a melt
transition. One official EOF is retried after a 5 second sleep. Headless
`B173WireClient` is the only client. There is no GUI and no Aero path.

Run directly with:

```text
java tools/smoke/RemainingSpongeGlassIceCycle.java m434-remaining-sponge-glass-ice
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`0716150d188414cd60d0bebe7aa70f27ace8a376a47f6e0a912fc026e8ab63b5`.
