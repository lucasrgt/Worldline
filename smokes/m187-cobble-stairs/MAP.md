<!-- worldline-map-schema=1 -->
<!-- boundary=m187-cobble-stairs -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=a569478836d30464768b7bc64d771b5cd735b7fb65e33ab5bb5a661e1f318a96 -->

# M187 behavior map

Packet15 places cobblestone stairs item `67` on a raised stone column.
Actor look Packet12 yaw `-90` causes the official server to write stairs
`67:0`. That exact east-facing cell survives a clean save plus fresh
login.

This milestone does not claim oak stairs `53`.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+cobblestairs67|cause=packet15-item67+look-90|wire=packet53-stairs67:0|oracle=look-facing-metadata+fresh-login|column=17,support=4:71:4:1:0,stairs=4:72:4:67:0,look=-90:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`a569478836d30464768b7bc64d771b5cd735b7fb65e33ab5bb5a661e1f318a96`.
