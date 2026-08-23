<!-- worldline-map-schema=1 -->
<!-- boundary=m200-brown-mushroom -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=4140e189cc2fa3f53a899ffa1b8332f24d0bf2a320fc2dc5f0050e9026718c70 -->

# M200 behavior map

Packet15 places brown mushroom item `39` on dirt inside a dark stone pocket.
The official server writes block `39:0`. That exact cell survives a clean
save plus fresh login.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-dirt3+dark-pocket|cause=packet15-item39|wire=packet53-brown-mushroom39:0|oracle=live-block39:0+fresh-login|column=17,dirt=4:72:4:3:0,mushroom=4:73:4:39:0,roof=4:74:4:1:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`4140e189cc2fa3f53a899ffa1b8332f24d0bf2a320fc2dc5f0050e9026718c70`.
