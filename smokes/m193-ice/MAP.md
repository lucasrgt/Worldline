# M193 behavior map

Packet15 places ice item `79` on a raised stone column. The official
server writes ice `79:0`. That exact cell survives a clean save plus
fresh login.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+ice79|cause=packet15-item79|wire=packet53-ice79:0|oracle=live-block79:0+fresh-login|column=17,support=4:71:4:1:0,ice=4:72:4:79:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`928502e6104af660eee12a0404bcc27b28b4d98e8da3440ba59e805f615f5c2a`.
