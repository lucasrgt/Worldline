<!-- worldline-map-schema=1 -->
<!-- boundary=m215-lapis-block -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=2eccf7bc9e04af5137d3804025114bea5686598ee5995596d8392a2a88b7fdbf -->

# M215 behavior map

Packet15 places lapis lazuli block item `22` on a raised stone column. The
official server writes lapis block `22:0`. That exact cell survives a clean
save plus fresh login.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+lapis22|cause=packet15-item22|wire=packet53-lapis22:0|oracle=live-block22:0+fresh-login|column=17,support=4:71:4:1:0,lapis=4:72:4:22:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`2eccf7bc9e04af5137d3804025114bea5686598ee5995596d8392a2a88b7fdbf`.
