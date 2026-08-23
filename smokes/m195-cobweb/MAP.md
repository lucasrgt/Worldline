<!-- worldline-map-schema=1 -->
<!-- boundary=m195-cobweb -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=4ebaa9934454eff6fdfeec745c7a31c093ebc734fd77ade5eac818c4c3c8531a -->

# M195 behavior map

Packet15 places cobweb item `30` on a raised stone column. The official
server writes cobweb `30:0`. That exact cell survives a clean save plus
fresh login. Slow-walk, shears, and string drops are not claimed.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+cobweb30|cause=packet15-item30|wire=packet53-cobweb30:0|oracle=live-block30:0+fresh-login|column=17,support=4:71:4:1:0,cobweb=4:72:4:30:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`4ebaa9934454eff6fdfeec745c7a31c093ebc734fd77ade5eac818c4c3c8531a`.
