# M174 behavior map

Packet15 places ladder item `65` against a raised stone east face. The
official server writes ladder `65:5`. That exact facing cell survives a
clean save plus fresh login.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+ladder65-east|cause=packet15-item65-place|wire=packet53-ladder65:5|oracle=side-facing-metadata+fresh-login|column=17,support=4:71:4:1:0,ladder=5:71:4:65:5,face=east,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`901e76a85d36008f4429e6863549902a4c2b49485980fbfb65a9568725bb491e`.
