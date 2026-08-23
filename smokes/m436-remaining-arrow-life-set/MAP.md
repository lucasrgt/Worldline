<!-- worldline-map-schema=1 -->
<!-- boundary=m436-remaining-arrow-life-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=9a370fd980f9abd2ed3f852ff575a9dae9c9b0f461c73fa548d131b40077011c -->

# M436 behavior map

A raised-stone pad seeds bow `261` and two arrows `262`. Packet15 air-use
of the bow emits Packet23 type `60` whose thrower is the actor. Packet14
then drops the remaining arrow stack so Packet21 item `262` appears. After
the official pickup delay, Packet103 restores arrow `262` to the hotbar.

This map does not re-qualify M332 workbench crafts or shoot-only type-60
spawn, M157 two-peer type-60 identity, or stuck-arrow `onCollideWithPlayer`
pickup of the Packet23 object. Headless `B173WireClient` only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+bow261+arrow262|cause=packet15-air-bow261+packet14-drop-262|wire=packet23-type60+packet21-262+packet103-collect-262|oracle=remaining-arrow-life-land-then-pickup|column=17,support=4:71:4:1:0,bow=261,arrow=262,wire=packet23-type60+packet21-262,thrower=actor,pickup=262,collect=true,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`9a370fd980f9abd2ed3f852ff575a9dae9c9b0f461c73fa548d131b40077011c`.
