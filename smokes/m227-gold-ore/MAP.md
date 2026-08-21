# M227 behavior map

Packet15 places gold ore item `14` on a raised stone column. The official
server writes gold ore `14:0`. That exact cell survives a clean save plus
fresh login.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+goldore14|cause=packet15-item14|wire=packet53-goldore14:0|oracle=live-block14:0+fresh-login|column=17,support=4:71:4:1:0,goldore=4:72:4:14:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`0cc34fc524f7aba7d51b5f354569bbbfa7bae8bde9995797972fac9dea8ba1fd`.
