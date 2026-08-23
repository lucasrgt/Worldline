<!-- worldline-map-schema=1 -->
<!-- boundary=m183-rails -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=189c56b02557d604b8886acbf0eb51505d54eab8cedbb903987462eebb7b3c46 -->

# M183 behavior map

Packet15 places rail item `66` on a raised stone column. Actor look Packet12
yaw `0` pins facing so the official server writes north-south rail `66:0`.
That exact cell survives a clean save plus fresh login.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+rail66|cause=packet15-item66+look0|wire=packet53-rail66:0|oracle=facing-metadata+fresh-login|column=17,support=4:71:4:1:0,rail=4:72:4:66:0,look=0:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`189c56b02557d604b8886acbf0eb51505d54eab8cedbb903987462eebb7b3c46`.
