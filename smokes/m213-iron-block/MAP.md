# M213 behavior map

Packet15 places iron block item `42` on a raised stone column. The official
server writes iron block `42:0`. That exact cell survives a clean save plus
fresh login.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+iron42|cause=packet15-item42|wire=packet53-iron42:0|oracle=live-block42:0+fresh-login|column=17,support=4:71:4:1:0,iron=4:72:4:42:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`f08a9fd9455cea30e230862721b12da696334532f35e67d3bcc977f3154ca81d`.
