# M317 qualification cycle

`SlowBlocksCycle` rebuilds the raised stone fixture in two fresh official server
JVMs, places cobweb item `30` and soul sand item `88`, verifies live Packet53
state, saves, and verifies both cells through a fresh client login. One official
EOF may be retried after five seconds.

The physics half is owned by `ClientCycle` and `B173PhysicsProbe.slowBlocks`.
That four-process differential compares the mapped and official player movement
roots on identical air, cobweb, and soul-sand corridors. M317 qualifies only
after both cycles have frozen matching evidence.

```text
java tools/harness/Gate.java --milestone m317-slow-blocks
java tools/harness/Gate.java --milestone controlled-client-tick
```

The server signature is
`bcae75456216b2655361256edd97079669619d908394782145a21a076e9e676a`.
The shared client-physics signature is
`c2508b3dfff5f7852ce6b3155c5257ba781482031001cfdd38326b3363a5c014`.
Headless protocol-14 only.
