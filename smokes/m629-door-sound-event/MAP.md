<!-- worldline-map-schema=1 -->
<!-- boundary=door-sound-event -->
<!-- nonclaims=client-audio-playback,attenuation,close-door-randomness,iron-doors,trapdoors,redstone-activation,other-world-events -->
<!-- frozen-trace=054db30511dff646e758a311a2c81378fab9e990e24bd4283c4d5b82e0c0320e -->

# M629 door sound event behavior map

The official Beta 1.7.3 dedicated server runs a fixed-seed raised wooden-door fixture with an
actor and a nearby observer. Before the action, the lower and upper cells are `64:0` and
`64:8`. The observer drains any earlier world events, then the actor activates the lower half
once with an empty hand.

The server must open the two cells to `64:4` and `64:12` for the actor and emit protocol-14
Packet61 effect `1003`, data `0`, at the exact lower-half position to the observer. The
reusable TestKit fixture rejects an event at either another position or another effect/data
pair and rejects either incomplete door-state transition.

Frozen signal:

```text
door=64:0/8->4/12,effect=1003:0,event-position=lower,packet=61,clients=2,disconnect=clean
```

This boundary does not claim local client-side audio playback or attenuation, close-door event
selection, iron doors, trapdoors, redstone activation, or any other Packet61 effect.

Frozen trace SHA-256: `054db30511dff646e758a311a2c81378fab9e990e24bd4283c4d5b82e0c0320e`.
