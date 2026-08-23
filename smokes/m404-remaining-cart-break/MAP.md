<!-- worldline-map-schema=1 -->
<!-- boundary=m404-remaining-cart-break -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=8a80558c9383a317d0d6a8f145c940ff21cb07ffb3649aa4c564214adde79bcf -->

# M404 behavior map

The fixture raises an isolated stone column and places two rails `66:0`.
Using minecart item `328` on the first rail emits Packet23 type `10`. Using
chest-minecart item `342` on the second isolated rail emits Packet23 type
`11`. Diamond sword `276` then Packet7-attacks (`leftClick=1`) each object
until Packet21 drops: type `10` yields minecart `328`, and type `11` yields
minecart `328` plus chest `54`.

This map does not re-qualify M155 type-`10` spawn, M311 chest-window /
furnace-cart interact, or M326 vehicle crafts. Headless `B173WireClient`
only. No GUI. No Aero.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-rail66-pair|cause=packet7-attack-type10+packet7-attack-type11|wire=packet23-type10+packet21-328+packet23-type11+packet21-328+packet21-54|oracle=remaining-cart-break-type10-328+type11-328-54|column=17,rail=4:72:4:66:0,cart=type10+thrower0+fixed144:2331:144,emptyDrop=packet21-328,chestRail=6:72:4:66:0,chest=type11+thrower0+fixed208:2331:144,chestDrops=packet21-328+packet21-54,sword=276,button=1,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`8a80558c9383a317d0d6a8f145c940ff21cb07ffb3649aa4c564214adde79bcf`.
