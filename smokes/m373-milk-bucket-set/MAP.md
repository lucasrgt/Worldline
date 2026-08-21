# M373 behavior map

One official session raises a `7×7` grass platform, places default mob
spawner `52`, and after a clean save rewrites the region NBT `EntityId`
from `Pig` to `Cow`. Packet7 button 0 while holding empty bucket `325`
on a living type-`92` cow fills milk bucket `335`. Packet15 direction-255
air-use then drinks that stack back to empty bucket `325`. Health stays
`20 -> 20`. The empty bucket survives a clean save plus fresh login.

This map does not re-qualify the shipping milk-drink-only trace (M267) or
the water/lava bucket-fluid family (M344). Headless `B173WireClient` only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-7x7-grass-platform+cow-spawner52+empty-bucket325|cause=packet7-button0-bucket325+packet15-dir255-bucket335|wire=packet24-type92+packet103-milk335+packet103-bucket325|oracle=live-fill-drink-325/335+fresh-login-empty-325|column=17,floor=4:71:4:1:0,platform=7x7-48grass,spawner=52:0-cow,mob=type92,fill=packet7-button0,drink=packet15-dir255,held=325:1:0->335:1:0->325:1:0,health=20->20,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`0def850e0165e277e1055538ab58e3a7772dcf0239f16acbc88f430b10e9a77c`.
