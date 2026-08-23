<!-- worldline-map-schema=1 -->
<!-- boundary=m177-painting -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=05ecb02dc2be9a42ab00eeae2c8c1eaf34609b0fe89c3c60aae4774b5e0e90d4 -->

# M177 behavior map

Painting item `321` is used through Packet15 on the west face of a raised
2x2 stone wall. Two peers decode the same protocol-14 Packet25 at
`5:72:4` direction `1`. Official art titles vary across JVMs, so the
frozen oracle is shared identity, type, and direction, not the RNG title.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-2x2-stone-wall|cause=packet15-item321-west|wire=packet25|oracle=two-peer-identical-painting-spawn|column=17,wall=5:72:4-5:73:5:1:0,painting=5:72:4:dir1,shared-title+shared-id,packet25,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`05ecb02dc2be9a42ab00eeae2c8c1eaf34609b0fe89c3c60aae4774b5e0e90d4`.
