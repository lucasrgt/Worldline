# M265 behavior map

Official raw fish item `349` is eaten with Packet15 air-use (direction `255`
at `-1,255,-1`). Vanilla Beta 1.7.3 has no hunger bar; raw fish is
`ItemFood` heal `+2`, capped at 20. The held stack is consumed `349:1 -> empty`.

This is not cooked fish item `350`. Cooked fish is a distinct `ItemFood` with
heal `+5` and is out of scope.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=fish349|cause=packet15-dir255-item349|wire=packet8-health18->20+packet103-fish-empty|oracle=itemfood-fish-heal2+stack-consume|health=18->20,heal=2,held=349:1->empty,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`0c9b15289f11f60a602735efc2cf64ae7cf2e4ad6454e33fd5fdb6a44023f832`.
