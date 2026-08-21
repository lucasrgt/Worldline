# M238 behavior map

Packet15 places grass item `2` on a raised stone column. The official
server writes grass `2:0`. That exact cell survives a clean save plus
fresh login.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+grass2|cause=packet15-item2|wire=packet53-grass2:0|oracle=live-block2:0+fresh-login|column=17,support=4:71:4:1:0,grass=4:72:4:2:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`3056478f663744b460245599a51d711c0d26a0d619c4595bd2def1f6cf3f99d4`.
