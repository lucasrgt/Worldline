<!-- worldline-map-schema=1 -->
<!-- boundary=m163-trapdoor -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=a93d386ba46f2b1dd44b2e91c9fc6c758d267a1b17cda83414f994a1d7d9d1a8 -->

# M163 behavior map

Trapdoor item 96 is placed against a raised stone east face as official
block `96`. Packet53 reports closed metadata `3`. Empty-hand Packet15 toggles
open `7`, then closed `3`. The closed cell survives save plus fresh login.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+trapdoor96-east|cause=packet15-item96-place+empty-hand-packet15-toggle|wire=packet53-trapdoor96:3->7->3|oracle=live-toggle+fresh-login-closed-trapdoor|column=17,support=4:71:4:1:0,trap=5:71:4:96:3->7->3,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`a93d386ba46f2b1dd44b2e91c9fc6c758d267a1b17cda83414f994a1d7d9d1a8`.
