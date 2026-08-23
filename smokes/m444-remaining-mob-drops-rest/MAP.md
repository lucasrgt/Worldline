<!-- worldline-map-schema=1 -->
<!-- boundary=m444-remaining-mob-drops-rest -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=4f0cf6fc97f045251947014072b407aae095b6419fb3c3ab94c50722f7db8f66 -->

# M444 behavior map

The fixture raises an isolated `7×7` grass platform and places two default
mob spawners `52:0`. After a clean save the first region NBT `EntityId` is
rewritten from `Pig` to `Sheep`; the second stays `Pig`. Packet7 with
diamond sword `276` kills Packet24 type `90` and type `91`. Packet21 must
include pork item `319` and undyed wool `35:0`. A bounded retry covers
vanilla `nextInt(3)` zero-drop outcomes without freezing drop count or
coordinates.

This map does not re-qualify M149 pig death, M150 pork `319` as a single
drop, M316/M406 living-sheep shears, M388 zombie feather `288` / skeleton
arrow `262`, M389 cow leather `334` / chicken feather `288`, M409 spider
string `287`, or M411 pigman cooked pork `320`. Zombie loot in Beta 1.7.3
is feather `288`, not rotten flesh. It does not claim cooked drops, XP,
breeding, or remaining creeper/ghast/slime loot.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-7x7-grass-platform+sheep-spawner52+pig-spawner52|cause=nbt-entityid-sheep+default-pig+official-diamond-sword-packet7|wire=packet24-type90+packet24-type91+packet38-status3+packet29+packet21-pork319+packet21-wool35:0|oracle=pig-pork-and-sheep-wool-death-drops-not-m388-zombie-feather|column=17,platform=7x7-48grass,sheep=4:72:4:52:0,pig=5:72:4:52:0,mobs=type90+type91,death=packet7-sword276+packet38-status3+packet29,drops=packet21-319+packet21-35:0,kills<=8,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`4f0cf6fc97f045251947014072b407aae095b6419fb3c3ab94c50722f7db8f66`.
