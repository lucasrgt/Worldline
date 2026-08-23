<!-- worldline-map-schema=1 -->
<!-- boundary=m242-lever-place -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=28f6b893342410779d684e2473574c663af5c667ff857e946aa225e71f8f69bf -->

# M242 behavior map

Packet15 places lever item `69` against a raised stone east face. The
official server writes unpowered lever `69:1`. That exact facing cell
survives a clean save plus fresh login. This is placement, not the M115
empty-hand toggle.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+lever69-east|cause=packet15-item69-place|wire=packet53-lever69:1|oracle=unpowered-facing-metadata+fresh-login|column=17,support=4:71:4:1:0,lever=5:71:4:69:1,face=east,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`28f6b893342410779d684e2473574c663af5c667ff857e946aa225e71f8f69bf`.
