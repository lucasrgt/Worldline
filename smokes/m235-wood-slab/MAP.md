<!-- worldline-map-schema=1 -->
<!-- boundary=m235-wood-slab -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=db22e6770987d78c1a09404991df3d73037770d68f7fb9215a20f0e9a4f383fa -->

# M235 behavior map

Packet15 places wood slab item `44` damage `2` on a raised stone column.
The official server writes single slab `44:2`. That exact cell survives a
clean save plus fresh login. This map does not re-qualify stone slab
`44:0`.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+slab44:2|cause=packet15-item44:2|wire=packet53-slab44:2|oracle=wood-metadata+fresh-login|column=17,support=4:71:4:1:0,slab=4:72:4:44:2,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`db22e6770987d78c1a09404991df3d73037770d68f7fb9215a20f0e9a4f383fa`.
