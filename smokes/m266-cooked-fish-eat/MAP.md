# M266 behavior map

Official cooked fish item `350` is eaten with Packet15 air-use (direction `255`
at `-1,255,-1`). Vanilla Beta 1.7.3 has no hunger bar; cooked fish is
`ItemFood` heal `+5`, capped at 20. The held stack is consumed `350:1 -> 0`.

This is not raw fish item `349`. Raw fish is a distinct `ItemFood` with heal
`+2` and is out of scope.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=cookedfish350|cause=packet15-dir255-item350|wire=packet8-health15->20+packet103-cookedfish-empty|oracle=itemfood-cookedfish-heal5+stack-consume|health=15->20,heal=5,cookedfish=350:1->0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`6a35349bc3363e2a0bdcba540cf2da951f99fef652b0ebf654ed56e15f0e168f`.
