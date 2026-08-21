# M186 behavior map

Packet15 places wooden stairs item `53` on a raised stone column. Actor look
Packet12 yaw `-90` causes the official server to write oak stairs `53:0`.
That exact cell survives a clean save plus fresh login.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+oakstairs53|cause=packet15-item53+look-90|wire=packet53-oakstairs53:0|oracle=look-facing-metadata+fresh-login|column=17,support=4:71:4:1:0,stairs=4:72:4:53:0,look=-90:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`2af3b75842248ca27774c84635352ca60069d3ae89a045bfbe93d37a926c2ccd`.
