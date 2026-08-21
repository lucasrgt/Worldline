# M438 behavior map

One official workbench epoch crafts clock `347` from four gold ingots
`266` around redstone `331`. Packet16 then holds that crafted clock.
Seeded empty map `358` is Packet15 air-used (direction `255` at
`-1,255,-1`). The official dedicated server does not fill that stack on
protocol-14: held remains `358:1:0 -> 358:1:0`. Clock `347` and the same
empty map persist across a clean save plus fresh login.

This map does not re-qualify M325's compass/clock/map crafts, M365's
compass needle, or M366's map-fill-only air-use. It does not invent a
filled-map damage. Headless `B173WireClient` protocol-14 only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=workbench58+gold266x4+redstone331x1+emptymap358|cause=packet102-craft-347+packet16-hold-347+packet15-dir255-item358|wire=result347+packet103-held-347+packet103-held-358:1:0|oracle=clock-craft-hold+map-air-use-unfilled+fresh-login-not-m365-not-m366|column=17,support=4:71:4:1:0,workbench=4:72:4:58:0,clock=347,held=347,map=358,filled=358:1:0->358:1:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`9ebe2cca746ab29d741407b8788d0b10a7e942cd691b868eb0d1d2f00e83eb58`.
