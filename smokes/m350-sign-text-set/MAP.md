# M350 behavior map

Packet15 places sign item `323` on a raised stone column as standing sign
`63:4`, then Packet15 against that stone east face as wall sign `68:5`.
Official Packet130 writes four lines onto each tile. Both tiles and both
texts are read back after a clean save plus fresh login. This is the M176
standing sign plus M245 wall sign compound, not either 1:1 smoke.

The frozen signal includes both block ids `63` and `68` plus the two
Packet130 texts.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+item323-block63+item323-block68|cause=packet15-item323-up+packet15-item323-east+packet130-ascii|wire=packet53-sign63:4+packet53-sign68:5+packet130-persist|oracle=fresh-login-packet130-63+packet130-68|column=17,support=4:71:4:1:0,standing=4:72:4:63:4,wall=5:71:4:68:5,text=Stand/sign/M350/ok+Wall/text/M350/ok,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`12d6f3d9302de6833a34efdedd9599e289de1ccf722ecd4cc8e32e8fad906d79`.
