# M374 qualification cycle

`RemainingFoodEatCycle` repeats the remaining ItemFood air-use family in
two fresh official server JVMs. Each run Packet15-eats apple `260`, cooked
pork `320`, and golden apple `322`, then reloads empty hotbar slots plus
health `20`. The signal must include all three consume transitions and the
`16 -> 20`, `12 -> 20`, and `10 -> 20` Packet8 path. It must not collapse
to M327 crafts or cake eat. One official EOF is retried after a 5 second
sleep. Headless `B173WireClient` is the only client. There is no GUI and
no Aero path.

Run directly with:

```text
java tools/smoke/RemainingFoodEatCycle.java m374-remaining-food-eat
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`8039053be1dc2477fd129e75dd6f6facd47634f0d8dc9e0be131b9750c9e2215`.
