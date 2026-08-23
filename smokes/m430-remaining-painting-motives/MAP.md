<!-- worldline-map-schema=1 -->
<!-- boundary=m430-remaining-painting-motives -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=1504c14913948dca32f92c0dacff830c42a51f7c402354b7a872fc92af410e09 -->

# M430 behavior map

Painting item `321` is used through Packet15 on three remaining west-face
stone walls that M177/M351's 2x2 surface cannot admit: 4x2, 4x3, and 4x4.
Two peers decode the same protocol-14 Packet25 triple at `5:72:5`,
`5:73:9`, and `5:73:13`, all direction `1`. Official art titles vary
across JVMs, so the frozen oracle is shared identity, type, one facing,
and the three remaining wall sizes, not the RNG titles.

This map is distinct from M351's west-plus-east 2x2 orientation family.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone-walls-4x2+4x3+4x4|cause=packet15-item321-4x2+packet15-item321-4x3+packet15-item321-4x4|wire=packet25+packet25+packet25|oracle=two-peer-identical-painting-spawns-remaining-sizes|column=17,wall4x2=5:72:4-5:73:7:1:0,art4x2=5:72:5:dir1,wall4x3=5:72:8-5:74:11:1:0,art4x3=5:73:9:dir1,wall4x4=5:72:12-5:75:15:1:0,art4x4=5:73:13:dir1,sizes=4x2+4x3+4x4,packet25+packet25+packet25,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`1504c14913948dca32f92c0dacff830c42a51f7c402354b7a872fc92af410e09`.
