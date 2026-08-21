# M245 behavior map

Packet15 places sign item `323` against a raised stone east face as wall
sign `68:5`. Official Packet130 then writes four UCS-2 lines. The same
tile text is read back after a clean save plus fresh login. This is not
standing sign `63`.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+item323-block68|cause=packet15-item323-east+packet130-ucs2|wire=packet53-sign68:5+packet130-persist|oracle=fresh-login-packet130|column=17,support=4:71:4:1:0,sign=5:71:4:68:5,text=Wall/sign/M245/ok,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`124ca56d12f9c02d6f8463c6ad28739dcc1c7b29875b4fa356570082c5f82c06`.
