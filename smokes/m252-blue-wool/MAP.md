<!-- worldline-map-schema=1 -->
<!-- boundary=m252-blue-wool -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=9df82196251a63865e986ad531cff422ad86ee987524f8c25679c38143ac80a3 -->

# M252 behavior map

Packet15 places blue wool item `35` with damage `11` on a raised stone
column. The official server writes wool `35:11`. That exact cell survives
a clean save plus fresh login.

This map does not re-qualify white wool `35:0`.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+wool35:11|cause=packet15-item35:11|wire=packet53-wool35:11|oracle=live-block35:11+fresh-login|column=17,support=4:71:4:1:0,wool=4:72:4:35:11,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`9df82196251a63865e986ad531cff422ad86ee987524f8c25679c38143ac80a3`.
