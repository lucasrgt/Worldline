# M424 qualification cycle

`FurnaceCartMotionSetCycle` rebuilds the raised stone track in two fresh
official server JVMs. Each run places rail item `66`, a second `66` cell one
cell south, detector rail item `28` one cell south of that, and
furnace-minecart item `343` on the first rail. The unfueled hold keeps
detector `28:0` idle. Packet7 of coal `263` from north of the cart then
consumes the stack and the cart activates detector `28:8` after traveling on
rail `66`. The result is distinct from M257 spawn-only type `12` and M377
powered-rail type `10`. One official EOF is retried after a 5 second sleep.
Headless `B173WireClient` is the only client. There is no GUI and no Aero
path.

Run directly with:

```text
java tools/smoke/FurnaceCartMotionSetCycle.java m424-furnace-cart-motion-set
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`536398b8e8c64ca3dc8e527842ae556bf4175363fc0b8e554d2ba0ec52811b1b`.
