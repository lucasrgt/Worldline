# M212 behavior map

Packet15 places gold block item `41` on a raised stone column. The official
server writes gold block `41:0`. That exact cell survives a clean save plus
fresh login. Gold ingot crafting is not claimed.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+goldblock41|cause=packet15-item41|wire=packet53-goldblock41:0|oracle=live-block41:0+fresh-login|column=17,support=4:71:4:1:0,goldblock=4:72:4:41:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`c41efdd35e74da0cb05078664f13008bcf8d47032c07fa0360ff0f4e57b9a9ce`.
