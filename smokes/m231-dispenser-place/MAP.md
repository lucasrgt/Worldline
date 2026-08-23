<!-- worldline-map-schema=1 -->
<!-- boundary=m231-dispenser-place -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=0679d7a20880a59f567737898c4d0263b285b13b738810981188b7b8b8fbecf4 -->

# M231 behavior map

Packet15 places dispenser item `23` on a raised stone column. Actor look
Packet12 yaw `180` causes the official server to write dispenser `23:3`.
That exact cell survives a clean save plus fresh login.

This map does not claim lever-powered eject (M153).

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+dispenser23|cause=packet15-item23+look180|wire=packet53-dispenser23:3|oracle=look-facing-metadata+fresh-login|column=17,support=4:71:4:1:0,dispenser=4:72:4:23:3,look=180:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`0679d7a20880a59f567737898c4d0263b285b13b738810981188b7b8b8fbecf4`.
