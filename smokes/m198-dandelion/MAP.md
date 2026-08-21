# M198 behavior map

Packet15 places yellow flower item `37` on dirt `3` capping the M175 raised
stone column. The official server writes dandelion `37:0`. That exact cell
survives a clean save plus fresh login.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-dirt3+dandelion37|cause=packet15-item37|wire=packet53-dandelion37:0|oracle=live-block37:0+fresh-login|column=17,dirt=4:72:4:3:0,dandelion=4:73:4:37:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`616709e090098e93e2e1928b9cde1a0122d5145752b870feee73266b32ce82cd`.
