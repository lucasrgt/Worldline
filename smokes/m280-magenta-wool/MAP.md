# M280 behavior map

Packet15 places magenta wool item `35` damage `2` on a raised stone
column. The official server writes wool `35:2`. That exact cell survives
a clean save plus fresh login.

This path is distinct from M197 white wool `35:0` and M248 orange wool
`35:1`. This map does not re-qualify those colors or other dyes.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+wool35:2|cause=packet15-item35:2|wire=packet53-wool35:2|oracle=live-block35:2+fresh-login|column=17,support=4:71:4:1:0,wool=4:72:4:35:2,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`1c2065b1a6b6a8fdbe04e1a4ed0e9d52b6fa44e7fc16fd931de75c89e017e1fe`.
