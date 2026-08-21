# M188 behavior map

Packet15 places stone slab item `44` on a raised stone column. The official
server writes single slab `44:0`. That exact cell survives a clean save
plus fresh login.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+slab44|cause=packet15-item44|wire=packet53-slab44:0|oracle=stone-metadata+fresh-login|column=17,support=4:71:4:1:0,slab=4:72:4:44:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`58238d33d76d1cd336bbd528ecfe662b6e821d1118c643e6c3023f1b18c800f1`.
