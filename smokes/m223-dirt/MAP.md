# M223 behavior map

Packet15 places dirt item `3` on a raised stone column. The official
server writes dirt `3:0`. That exact cell survives a clean save plus
fresh login.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+dirt3|cause=packet15-item3|wire=packet53-dirt3:0|oracle=live-block3:0+fresh-login|column=17,support=4:71:4:1:0,dirt=4:72:4:3:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`cb51b4a07fd7c818ad09e7ea60fe06e3c01a3f3bca23a3c618cfe10d5a5cb650`.
