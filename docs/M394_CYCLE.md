# M394 qualification cycle

`RemainingSlabPlaceCycle` rebuilds the raised stone pads in two fresh
official server JVMs. Each run places sandstone slab `44:1`, wood slab
`44:2`, cobble slab `44:3`, and double slab `43:0`, then reloads those
cells after save plus fresh login. The signal must include multiple `44`
damages. One official EOF is retried after a 5 second sleep. Headless
`B173WireClient` is the only client. There is no GUI and no Aero path.

Run directly with:

```text
java tools/smoke/RemainingSlabPlaceCycle.java m394-remaining-slab-place
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`7939076b43b10ef5972487f306388abb58dafba8b1ca28923a3fb952ef2c6a9f`.
