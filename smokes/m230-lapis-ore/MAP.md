<!-- worldline-map-schema=1 -->
<!-- boundary=m230-lapis-ore -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=f684efb5f52db98a991a7edd381072bb58cc997a3d3bfd07327725c2e5026139 -->

# M230 behavior map

Packet15 places lapis ore item `21` on a raised stone column. The official
server writes lapis ore `21:0`. That exact cell survives a clean save plus
fresh login.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+lapis21|cause=packet15-item21|wire=packet53-lapis21:0|oracle=live-block21:0+fresh-login|column=17,support=4:71:4:1:0,lapis=4:72:4:21:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`f684efb5f52db98a991a7edd381072bb58cc997a3d3bfd07327725c2e5026139`.
