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
java tools/smoke/SlowBlocksCycle.java m317-slow-blocks
java tools/smoke/ClientCycle.java controlled-client-tick
```

The replacement signatures remain pending. Headless protocol-14 only.
