# M259 behavior map

Official cooked pork item `320` is seeded below full health and eaten with
Packet15 direction `255`. Vanilla cooked pork restores eight health points,
capped at 20. The stack is consumed. Beta 1.7.3 has no hunger bar.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+cookedpork320|cause=packet15-dir255-item320|wire=packet8-health12->20+packet103-empty-hotbar1|oracle=cooked-pork-heal8+stack-consume+fresh-login|column=17,support=4:71:4:1:0,health=12->20,heal=8,item=320:1:0->empty,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`c287f963780070b55c9773bcc0ad5b914a8c6a7713870dd9d5533eda3d449b0e`.
