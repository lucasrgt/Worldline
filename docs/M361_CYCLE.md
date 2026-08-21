# M361 qualification cycle

`LadderClimbSetCycle` rebuilds the raised two-cell ladder fixture in two
fresh official server JVMs. Each run places ladder item `65` as two east
`65:5` cells, Packet13-climbs at least two cells of height, and contrasts
that pose delta with the air column without ladder. One official EOF is
retried after a 5 second sleep.

The frozen semantic SHA-256 is
`113dccdda9b6bd0140c7aea5b255db993bb9063c6d64ef9370f1fb9925c26340`.

Run directly with:

```text
java tools/smoke/LadderClimbSetCycle.java m361-ladder-climb-set
```

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
