<!-- worldline-map-schema=1 -->
<!-- boundary=m201-red-mushroom -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=26885845d6ad7c99bd324b497cb5415fe9056a79b295d940bc26da6049f0848a -->

# M201 behavior map

Packet15 places red mushroom item `40` on dirt inside a dark stone pocket.
The official server writes `40:0`. That exact cell survives a clean save
plus fresh login.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=dirt3+dark-pocket+mushroom40|cause=packet15-item40|wire=packet53-mushroom40:0|oracle=live-block40:0+fresh-login|column=17,dirt=4:72:4:3:0,roof=4:74:4:1:0,mushroom=4:73:4:40:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`26885845d6ad7c99bd324b497cb5415fe9056a79b295d940bc26da6049f0848a`.
