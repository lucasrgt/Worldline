<!-- worldline-map-schema=1 -->
<!-- boundary=m182-redstone-torch -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=3e8129618cb8674fb0d5a7580c16d55d100162fde4602e17ac89b0af9fdd5d4a -->

# M182 behavior map

Packet15 places redstone torch item `76` on a raised stone column. The
official server writes floor redstone torch `76:5`. That exact cell survives
a clean save plus fresh login.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+torch76|cause=packet15-item76|wire=packet53-torch76:5|oracle=floor-metadata+fresh-login|column=17,support=4:71:4:1:0,torch=4:72:4:76:5,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`3e8129618cb8674fb0d5a7580c16d55d100162fde4602e17ac89b0af9fdd5d4a`.
