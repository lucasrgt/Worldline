<!-- worldline-map-schema=1 -->
<!-- boundary=m429-remaining-attach-faces -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=d00079b30c3f58f9f2a197e5a0a27c88880e15c28c3eaf88806d4502ebc2eb2b -->

# M429 behavior map

One official session places remaining wall-attach facings of ladder `65`,
trapdoor `96`, and wall sign `68` on one three-cell raised stone column.
Packet15 of ladder item `65` writes west `65:4`, south `65:3`, and north
`65:2`. Packet15 of trapdoor item `96` writes west `96:2`, south `96:1`,
and north `96:0` closed. Packet15 of sign item `323` writes west `68:4`,
south `68:3`, and north `68:2`. East faces stay air. All nine cells
survive a clean save plus fresh login.

This map does not re-qualify east ladder climb (M361), trapdoor open/close
(M380), or Packet130 sign text (M350). Headless `B173WireClient` only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone-column3+ladder65:4+65:3+65:2+trapdoor96:2+96:1+96:0+item323-wall68:4+68:3+68:2|cause=packet15-item65-west+south+north+packet15-item96-west+south+north+packet15-item323-west+south+north|wire=packet53-ladder65:4+65:3+65:2+trapdoor96:2+96:1+96:0+sign68:4+68:3+68:2|oracle=remaining-wall-attach-faces+fresh-login|column=19,low=4:71:4:1:0,mid=4:72:4:1:0,high=4:73:4:1:0,ladder=3:71:4:65:4+4:71:5:65:3+4:71:3:65:2,trapdoor=3:72:4:96:2+4:72:5:96:1+4:72:3:96:0,wallsign=3:73:4:68:4+4:73:5:68:3+4:73:3:68:2,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`d00079b30c3f58f9f2a197e5a0a27c88880e15c28c3eaf88806d4502ebc2eb2b`.
