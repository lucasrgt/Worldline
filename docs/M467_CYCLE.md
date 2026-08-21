# M467 qualification cycle

`DifficultyDamageSetCycle` rebuilds the raised `7×7` grass zombie pad in
two fresh official server JVMs. Each run boots Easy `difficulty=1` then
Hard `difficulty=3`, reseeds Health `20` between those inner boots, and
waits for Packet24 type `54` melee Packet8. One official EOF is retried
after a 5 second sleep. Station movement uses a 9-tick cap.

The frozen signal must name type `54`, `difficulty=1+3`, Packet8
`20->18` on both boots, and `armor=none`. It must not collapse to M451
armor reduction, M454 peaceful despawn, or M446 door break.

Run directly with:

```text
java tools/smoke/DifficultyDamageSetCycle.java m467-difficulty-damage-set
```

The frozen semantic SHA-256 is
`61e1ac15b1e84c70af6ec58f615e81db3d5a6ae0c3deaac931da803a16f459d7`.

Canonical evidence uses two official server JVMs of the full Easy-then-Hard
family. Headless protocol-14 only. No GUI. No Aero.
