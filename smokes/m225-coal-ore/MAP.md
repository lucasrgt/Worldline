<!-- worldline-map-schema=1 -->
<!-- boundary=m225-coal-ore -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=2ff6ed07ba198e90c82b36fb04ced28cc6e6900e1ccca71d4ee74554803e53fd -->

# M225 behavior map

Packet15 places coal ore item `16` on a raised stone column. The official
server writes coal ore `16:0`. That exact cell survives a clean save plus
fresh login.

This map does not claim mining drops.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+ore16|cause=packet15-item16|wire=packet53-ore16:0|oracle=live-block16:0+fresh-login|column=17,support=4:71:4:1:0,ore=4:72:4:16:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`2ff6ed07ba198e90c82b36fb04ced28cc6e6900e1ccca71d4ee74554803e53fd`.
