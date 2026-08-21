# M204 behavior map

Packet15 places clay item `82` on a raised stone column. The official
server writes clay `82:0`. That exact cell survives a clean save plus
fresh login.

This map does not claim clay balls, brick smelting, or water generation.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+clay82|cause=packet15-item82|wire=packet53-clay82:0|oracle=live-block82:0+fresh-login|column=17,support=4:71:4:1:0,clay=4:72:4:82:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`3b76da4b3617c14a154aaf9799e7cd86631ba7e4e9be1d8dd1baf628fb271d8c`.
