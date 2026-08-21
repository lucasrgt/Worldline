# M192 behavior map

Packet15 places soul sand item `88` on a raised stone column. The official
server writes Overworld soul sand `88:0`. That exact cell survives a clean
save plus fresh login.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+soulsand88|cause=packet15-item88|wire=packet53-soulsand88:0|oracle=live-block88:0+fresh-login|column=17,support=4:71:4:1:0,soulsand=4:72:4:88:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`4cf9190cca5bf84eabf13581b40e3e944a7c81c6c62f80bea686b7ff436ea63e`.
