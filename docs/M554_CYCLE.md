# M554 qualification cycle

`ExtendedHeadBreakSetCycle` rebuilds the cloned piston-`33` west arm in
two fresh official server JVMs. Each run extends piston `33` until head
`34` is present, Packet14-breaks the extended BASE with iron pickaxe
`257`, and reloads the leftover air cells after save plus fresh login.
The frozen signal includes `extend` and `head-break` and must not include
`retract`. One official EOF is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`2cc464442cf4d3f0a5f88c7cb81921c7594834d6c9114630b54798241b4c5cbf`.

Run directly with:

```text
java tools/smoke/ExtendedHeadBreakSetCycle.java m554-extended-head-break-set
```

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
