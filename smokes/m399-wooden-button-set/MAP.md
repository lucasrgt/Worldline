# M399 behavior map

Beta 1.7.3 has no wooden button (`143` is later). This map claims the
remaining stone-button wall-attachment family as a SET. Packet15 of item
`77` places `77:1` on the east face of a raised stone column, `77:2` on
the west face, `77:3` on the south face, and `77:4` on the north face.
A clean save plus fresh login keeps all four unpowered.

This map does not re-qualify the M165/M279 east-face pulse or the M340
lever-plus-one-button pulse. It does not invent wooden buttons.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+button77-east-west-south-north|cause=packet15-item77-east+west+south+north|wire=packet53-button77:1+77:2+77:3+77:4|oracle=wall-attachment-metadata-set+fresh-login|column=17,support=4:71:4:1:0,east=5:71:4:77:1,west=3:71:4:77:2,south=4:71:5:77:3,north=4:71:3:77:4,look=-90+90+0+180,persisted=77:1+77:2+77:3+77:4,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`898b58fa0f849df159f7bfcfde243b0957fddcd580770518251b1721cbf21c90`.
