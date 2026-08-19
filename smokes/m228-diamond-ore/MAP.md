# M228 behavior map

Packet15 places diamond ore item `56` on a raised stone column. The official
server writes diamond ore `56:0`. That exact cell survives a clean save plus
fresh login.

This map does not claim natural diamond-ore generation, diamond gem drops,
or silk-touch.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+ore56|cause=packet15-item56|wire=packet53-ore56:0|oracle=live-block56:0+fresh-login|column=17,support=4:71:4:1:0,ore=4:72:4:56:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`d8899da5d17c18d27351804645eead6a4a792f1d0497886d4bd24a15bedef72b`.
