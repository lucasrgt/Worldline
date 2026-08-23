<!-- worldline-map-schema=1 -->
<!-- boundary=m233-note-block-place -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=7b80c1a46b0ca115b927e8ef216452351d1c9bcef2ca26a49e5ab4dc6abedcc9 -->

# M233 behavior map

Packet15 places note block item `25` on a raised stone column. The official
server writes note block `25:0`. That exact cell survives a clean save plus
fresh login. This map does not click the note or play Packet54.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+noteblock25|cause=packet15-item25|wire=packet53-noteblock25:0|oracle=live-block25:0+fresh-login|column=17,support=4:71:4:1:0,noteblock=4:72:4:25:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`7b80c1a46b0ca115b927e8ef216452351d1c9bcef2ca26a49e5ab4dc6abedcc9`.
