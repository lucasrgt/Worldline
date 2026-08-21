# M317 qualification cycle

`SlowBlocksCycle` rebuilds the raised stone cobweb and soul-sand path in
two fresh official server JVMs. Each run places cobweb item `30` and soul
sand item `88`, Packet13-walks into those cells, and freezes
air-versus-cobweb and air-versus-soul-sand standing pose deltas over
eight ticks. One official EOF is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`bcae75456216b2655361256edd97079669619d908394782145a21a076e9e676a`.

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.

```text
java tools/smoke/SlowBlocksCycle.java m317-slow-blocks
```
