# M237 behavior map

Packet15 places stone item `1` on a raised stone column. The official
server writes stone `1:0`. That exact cell survives a clean save plus
fresh login.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+stone1|cause=packet15-item1|wire=packet53-stone1:0|oracle=live-block1:0+fresh-login|column=17,support=4:71:4:1:0,stone=4:72:4:1:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`a8969e296f04e9e9e445c08139a0fd689dc08bc6796515a90ad78d5b8e4f3ee9`.
