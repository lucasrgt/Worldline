# M361 qualification cycle

`LadderClimbSetCycle` rebuilds the raised two-cell fixture in two fresh official
server JVMs, places ladder item `65` as two east-facing `65:5` cells, verifies
live Packet53 state, saves, and verifies both cells through a fresh client
login. One official EOF may be retried after five seconds.

The physics half is owned by `ClientCycle` and `B173PhysicsProbe.ladder`. That
four-process differential compares mapped and official player movement roots
on identical wall-air and two-cell-ladder fixtures. M361 qualifies only after
both cycles have frozen matching evidence.

```text
java tools/smoke/LadderClimbSetCycle.java m361-ladder-climb-set
java tools/smoke/ClientCycle.java controlled-client-tick
```

The replacement signatures remain pending. Headless protocol-14 only.
