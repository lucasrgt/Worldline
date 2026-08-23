<!-- worldline-map-schema=1 -->
<!-- boundary=m443-remaining-bucket-rest-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=b556b71fd57896aa06fbb39f5088d8f96e6c8a64076014c7d7391b961c669eb7 -->

# M443 behavior map

One official session builds two raised stone trenches and seeds still water
`9:0` and still lava `11:0` behind dirt gates. Packet14 opens each gate onto
air. Official fluid updates then publish flowing water `9:1` and flowing lava
`11:2` while both sources remain still. Empty bucket `325` Packet15
direction-255 then rejects each flowing cell and picks up each source: water
source `9:0` becomes air and slot `325` becomes water bucket `326`; lava
source `11:0` becomes air and slot `325` becomes lava bucket `327`. Flowing
cells stay `9:1` and `11:2`. The empty sources and both filled buckets survive
a clean save plus fresh login.

M344 already froze still-source place plus pickup for `326/9` and `327/11`.
This map is the remaining source-versus-flowing pickup contrast, not another
place-plus-pickup. Headless `B173WireClient` only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone-trenches+seeded-still-water9+seeded-still-lava11+dirt-gates3+empty-bucket325x2|settle=40+70ticks|cause=packet15-dir255-bucket325-flowing-then-source|wire=packet53-flow-keep+packet103-bucket325+packet53-air0+packet103-bucket326+packet53-flow-keep+packet103-bucket325+packet53-air0+packet103-bucket327|oracle=source-vs-flowing-pickup-8/9+10/11-not-m344-place-pickup|column=17,water-source=4:72:4:9:0->0:0,water-flow=5:72:4:9:1->9:1,held-water=325:1:0->325:1:0->326:1:0,lava-source=4:72:8:11:0->0:0,lava-flow=5:72:8:11:2->11:2,held-lava=325:1:0->325:1:0->327:1:0,water=8/9,lava=10/11,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`b556b71fd57896aa06fbb39f5088d8f96e6c8a64076014c7d7391b961c669eb7`.
