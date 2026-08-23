<!-- worldline-map-schema=1 -->
<!-- boundary=m261-golden-apple-eat -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=888e7fe8215ede44deaf9a73fa95ecc769f61e554e9262d0781ae75eca2e9fe3 -->

# M261 behavior map

Official golden apple item `322` is seeded in hotbar slot 0 with Health `10`.
Packet15 direction 255 eats the stack in air. Packet8 restores health
`10 -> 20` (full heal, item heal 20 capped at 20) and Packet103 empties
slot 36. Regular apple item `260` is out of scope.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=seeded-item322+health10|cause=packet15-direction255-item322|wire=packet8-health10->20+packet103-slot36-empty|oracle=golden-apple-full-heal+stack-consume+fresh-login|chunk=0:0,health=10->20,heal=20,item=322:1:0->empty,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`888e7fe8215ede44deaf9a73fa95ecc769f61e554e9262d0781ae75eca2e9fe3`.
