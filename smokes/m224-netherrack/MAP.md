<!-- worldline-map-schema=1 -->
<!-- boundary=m224-netherrack -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=790c2fc12c97371a7b0a14f5a41376c1d23f3bd1fff998120baed91087fd917b -->

# M224 behavior map

Packet15 places netherrack item `87` on a raised stone column. The official
server writes Overworld netherrack `87:0`. That exact cell survives a clean
save plus fresh login.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+netherrack87|cause=packet15-item87|wire=packet53-netherrack87:0|oracle=live-block87:0+fresh-login|column=17,support=4:71:4:1:0,netherrack=4:72:4:87:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`790c2fc12c97371a7b0a14f5a41376c1d23f3bd1fff998120baed91087fd917b`.
