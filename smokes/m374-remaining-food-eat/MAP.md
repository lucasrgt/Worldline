<!-- worldline-map-schema=1 -->
<!-- boundary=m374-remaining-food-eat -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=8039053be1dc2477fd129e75dd6f6facd47634f0d8dc9e0be131b9750c9e2215 -->

# M374 behavior map

Official apple `260`, cooked pork `320`, and golden apple `322` are eaten
as one Packet15 air-use family. Packet15 direction 255 eats each stack:
Packet8 restores `16 -> 20` (apple heal 4), `12 -> 20` (cooked pork heal
8), and `10 -> 20` (golden apple heal 20, cap 20), and Packet103 consumes
each hotbar stack to empty. Beta 1.7.3 has no hunger bar; food heals
health. Food `maxStackSize` is 1.

This map does not re-qualify M327 food crafts or cake eat (M160 / M335 /
M369). Headless `B173WireClient` only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=apple260+cookedpork320+golden322|cause=packet15-dir255-item260+packet15-dir255-item320+packet15-dir255-item322|wire=packet8-health16->20+12->20+10->20+packet103-empty-260+320+322|oracle=itemfood-apple-heal4+pork-heal8+golden-heal20+stack-consume+fresh-login|apple=260:1:0->empty,health=16->20,heal=4,pork=320:1:0->empty,health=12->20,heal=8,golden=322:1:0->empty,health=10->20,heal=20,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`8039053be1dc2477fd129e75dd6f6facd47634f0d8dc9e0be131b9750c9e2215`.
