# M362 qualification cycle

`FenceCollisionSetCycle` rebuilds the raised stone fence path in two
fresh official server JVMs. Each run places two adjacent fence items
`85` as `85:0`, Packet13-walks the same intended step in air and into
that fence line, and freezes air-versus-fence standing pose evidence.
One official EOF is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`5784076d8eb5c6e86478f102566067459f9c73c231b5f92141b25d65c79ae290`.

Canonical evidence uses two official server JVMs and four client
sessions. Headless protocol-14 only. No GUI. No Aero.

```text
java tools/smoke/FenceCollisionSetCycle.java m362-fence-collision-set
```
