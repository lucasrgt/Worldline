# M397 qualification cycle

`DispenserProjectilesCycle` rebuilds the raised west-facing dispenser
fixture in two fresh official server JVMs. Each run places dispenser `23`,
loads snowball `332` and egg `344` through the Trap window, and ejects both
stacks with side-lever pulses. The existing object tracker awaits Packet23
types `61` and `62`. One official EOF is retried after a 5 second sleep.

Run directly with:

```text
java tools/smoke/DispenserProjectilesCycle.java m397-dispenser-projectiles
```

Canonical evidence uses two official server JVMs and two client sessions.
Headless protocol-14 only. No GUI. No Aero. The frozen semantic SHA-256 is
`66d497bee36abdc673c44336dad9a75afcc08fcf7ade36676c652023100b1731`.
