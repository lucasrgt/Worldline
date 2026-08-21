# M439 behavior map

One official session Packet15-places remaining ores coal `16`, lapis `21`,
and redstone `73` on a raised three-pad stone fixture. Coal ore item `16`
writes `16:0` on the center support. Lapis ore item `21` writes `21:0` on
the east pad. Redstone ore item `73` writes unlit `73:0` on the west pad.
All three cells survive a clean save plus fresh login.

This map is distinct from M225/M230/M229 1:1 ore places and from M300/M375
pick harvests. It does not claim iron, gold, or diamond ore, glowing ore
`74`, or Packet14/Packet21 drops. Headless `B173WireClient` only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+ore16+ore21+ore73|cause=packet15-item16+item21+item73|wire=packet53-ore16:0+ore21:0+ore73:0|oracle=ore-place-family+fresh-login|column=17,support=4:71:4:1:0,coal=4:72:4:16:0,lapis=5:72:4:21:0,redstone=3:72:4:73:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`0c58ca403f7064fde875a5257d07193fe9916277c21455b47ac366ab28b828ab`.
