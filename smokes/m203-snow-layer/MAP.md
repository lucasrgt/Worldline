# M203 behavior map

Packet15 places snow layer item `78` on a raised stone column. The official
server writes snow layer `78:0`. That exact cell survives a clean save plus
fresh login. Snow block `80`, snowfall, and melting are not claimed.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+snowlayer78|cause=packet15-item78|wire=packet53-snowlayer78:0|oracle=live-block78:0+fresh-login|column=17,support=4:71:4:1:0,snowlayer=4:72:4:78:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`23163bc78dd8ce876aff379a6292ad170fe0b74e5c26f2ee7177508222aa0178`.
