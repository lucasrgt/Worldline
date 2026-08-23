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
java tools/harness/Gate.java --milestone m361-ladder-climb-set
java tools/harness/Gate.java --milestone controlled-client-tick
```

The server signature is
`113dccdda9b6bd0140c7aea5b255db993bb9063c6d64ef9370f1fb9925c26340`.
The shared client-physics signature is
`c2508b3dfff5f7852ce6b3155c5257ba781482031001cfdd38326b3363a5c014`.
Headless protocol-14 only.
