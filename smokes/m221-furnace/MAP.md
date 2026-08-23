<!-- worldline-map-schema=1 -->
<!-- boundary=m221-furnace -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=88ee8a957cbe3c4c1f6f27f8b4fa73786709b04be0e37ea4c9b50e01737b965c -->

# M221 behavior map

Packet15 places furnace item `61` on a raised stone column. The official
server writes idle furnace `61:2`. That exact unlit cell survives a clean
save plus fresh login.

This milestone does not smelt and does not claim burning furnace `62`.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+furnace61|cause=packet15-item61|wire=packet53-furnace61:2|oracle=idle-metadata+fresh-login|column=17,support=4:71:4:1:0,furnace=4:72:4:61:2,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`88ee8a957cbe3c4c1f6f27f8b4fa73786709b04be0e37ea4c9b50e01737b965c`.
