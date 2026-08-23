<!-- worldline-map-schema=1 -->
<!-- boundary=m258-bread-eat -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=1f0cbe46155bbaf393891dc8f4343effa6b5f502c8efc4c8f0122c424a05da3a -->

# M258 behavior map

Official bread item `297` is eaten with Packet15 air-use (direction `255` at
`-1,255,-1`). Vanilla Beta 1.7.3 has no hunger bar; bread heals five health
points, capped at 20. The held stack is consumed `297:1 -> 0`.

This is not a cake bite. Cake uses empty-hand Packet15 on BlockCake `92`.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=bread297|cause=packet15-dir255-item297|wire=packet8-health15->20+packet103-bread-empty|oracle=itemfood-bread-heal5+stack-consume|health=15->20,heal=5,bread=297:1->0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`1f0cbe46155bbaf393891dc8f4343effa6b5f502c8efc4c8f0122c424a05da3a`.
