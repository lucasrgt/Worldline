# M260 behavior map

Official apple item 260 is seeded with health 16. Packet15 air-use
(direction 255) eats one apple: Packet8 restores `16 -> 20` (four points,
capped at 20) and Packet103 consumes the hotbar stack. Beta 1.7.3 has no
hunger bar; apples heal health. Food `maxStackSize` is 1.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+apple260|cause=packet15-direction255-item260|wire=packet8-health16->20+packet103-apple-empty|oracle=apple-heal4-cap20+stack-consume|column=17,support=4:71:4:1:0,health=16->20,heal=4,held=260:1:0->empty,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`f5122f857247406ea443e90df7cb0b2f8b8bfd0ef8f151b677d9b3f8a4598130`.
