# M350 qualification cycle

`SignTextSetCycle` rebuilds the raised stone fixture in two fresh official
server JVMs. Each run places item `323` as standing sign `63` and wall
sign `68`, writes Packet130 lines onto both tiles, and reloads both texts
after save plus fresh login. The frozen signal includes both block ids
`63` and `68` plus the Packet130 text. Headless `B173WireClient` is the
only client. There is no GUI and no Aero path. One official EOF is
retried after a 5 second sleep.

Run directly with:

```text
java tools/smoke/SignTextSetCycle.java m350-sign-text-set
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`12d6f3d9302de6833a34efdedd9599e289de1ccf722ecd4cc8e32e8fad906d79`.
