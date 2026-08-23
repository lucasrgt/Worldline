<!-- worldline-map-schema=1 -->
<!-- boundary=m389-animal-drops-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=761e3177132b22cd98c5dd6a4fa802903098e923c7e0c31115dfae512c06213b -->

# M389 behavior map

The fixture raises an isolated `7×7` grass platform and places two default
mob spawners `52:0`. After a clean save the region NBT `EntityId` values are
rewritten from `Pig` to `Cow` then `Chicken`. Packet7 with diamond sword
`276` kills Packet24 type `92` and type `93`. Packet21 must include leather
item `334` and feather item `288`. A bounded retry covers vanilla
`nextInt(3)` zero-drop outcomes without freezing drop count or coordinates.

This map does not re-qualify M149 pig death or M150 pork `319`. It does not
claim cooked drops, XP, breeding, milk, or hostile loot.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-7x7-grass-platform+cow-spawner52+chicken-spawner52|cause=official-diamond-sword-packet7|wire=packet24-type92+packet24-type93+packet38-status3+packet29+packet21-leather334+packet21-feather288|oracle=cow-leather-and-chicken-feather-drops|column=17,platform=7x7-48grass,cow=4:72:4:52:0,chicken=5:72:4:52:0,mobs=type92+type93,death=packet7-sword276+packet38-status3+packet29,drops=packet21-334+packet21-288,kills<=8,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`761e3177132b22cd98c5dd6a4fa802903098e923c7e0c31115dfae512c06213b`.
