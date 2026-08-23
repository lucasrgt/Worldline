<!-- worldline-map-schema=1 -->
<!-- boundary=m587-bow-shot-durability-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=caf95c2a196f0ce5fe2bf058118dda511ea834261ccf2075344d705ccb9ed5e0 -->

# M587 behavior map

A raised-stone column hosts seeded bow `261` plus one arrow `262`. Packet15
air-use emits Packet23 type `60` whose thrower is the actor. Arrow `262` is
consumed. The held bow remains `261` and Packet103 remaining durability
damage is reloaded after a clean save plus fresh login.

This map does not re-qualify M157 two-peer type-60 identity, M332 workbench
crafts, or M462 pig/zombie hits. Headless `B173WireClient` protocol-14 only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone-platform+bow261+arrow262|cause=packet15-air-bow261|wire=packet23-type60+packet103-261:0|oracle=held-bow-durability-not-m157-peer-or-m332-craft-or-m462-hit|column=17,support=4:71:4:1:0,bow=261:0,arrow=262:1->0,wire=packet23-type60,thrower=actor,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`caf95c2a196f0ce5fe2bf058118dda511ea834261ccf2075344d705ccb9ed5e0`.
