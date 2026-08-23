<!-- worldline-map-schema=1 -->
<!-- boundary=m262-cookie-eat -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=2374d08bdc38c0697e31d3009c028cbb6f70fb2794e2d20606804aaa4dbfb0bc -->

# M262 behavior map

Official cookie item `357` is eaten with Packet15 air-use (direction `255` at
`-1,255,-1`). Vanilla Beta 1.7.3 has no hunger bar; cookie heals one health
point, capped at 20. The held stack is consumed `357:1 -> empty`.

This is not a cake bite and not bread. Cake uses empty-hand Packet15 on
BlockCake `92`. Bread item `297` heals five points.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=cookie357|cause=packet15-dir255-item357|wire=packet8-health19->20+packet103-cookie-empty|oracle=itemfood-cookie-heal1+stack-consume|health=19->20,heal=1,cookie=357:1->empty,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`2374d08bdc38c0697e31d3009c028cbb6f70fb2794e2d20606804aaa4dbfb0bc`.
