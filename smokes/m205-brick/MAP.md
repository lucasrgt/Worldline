<!-- worldline-map-schema=1 -->
<!-- boundary=m205-brick -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=e086305d19013c746cd1e24ea91a5cf8c20cb87ac8b3e0d6c79d11b224a1ac90 -->

# M205 behavior map

Packet15 places brick block item `45` on a raised stone column. The official
server writes brick `45:0`. That exact cell survives a clean save plus
fresh login.

This map does not claim clay smelting or brick item `336`.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+brick45|cause=packet15-item45|wire=packet53-brick45:0|oracle=live-block45:0+fresh-login|column=17,support=4:71:4:1:0,brick=4:72:4:45:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`e086305d19013c746cd1e24ea91a5cf8c20cb87ac8b3e0d6c79d11b224a1ac90`.
