# M191 behavior map

Packet15 places glowstone item `89` on a raised stone column. The official
server writes Overworld glowstone `89:0`. That exact cell survives a clean
save plus fresh login.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+glowstone89|cause=packet15-item89|wire=packet53-glowstone89:0|oracle=live-block89:0+fresh-login|column=17,support=4:71:4:1:0,glowstone=4:72:4:89:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`3b17d9813ae06da188a84e5b3ea33feedbc0bc0f9c537be65e90e2dbf47f2187`.
