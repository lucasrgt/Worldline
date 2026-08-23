<!-- worldline-map-schema=1 -->
<!-- boundary=m366-map-fill-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=048613204222ae9dce7fb157d74dc94b69573ce8faaa9dd90cff64f7aab8f31f -->

# M366 behavior map

Official empty map item `358` is seeded into the hotbar and used with
Packet15 air-use (direction `255` at `-1,255,-1`) on the raised stone
fixture. Protocol-14 does not assign a filled-map damage/id: the held
stack stays `358:1:0 -> 358:1:0` and that same empty stack persists
across a clean save plus fresh login.

This is not M325. M325 only crafts empty map `358` from paper plus
compass. M366 air-uses an already seeded empty map and freezes the
official fill result, which is a no-op on protocol-14.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+emptymap358|cause=packet15-dir255-item358|wire=packet103-held-358:1:0|oracle=map-fill-air-use+fresh-login|column=17,support=4:71:4:1:0,map=358,filled=358:1:0->358:1:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`048613204222ae9dce7fb157d74dc94b69573ce8faaa9dd90cff64f7aab8f31f`.
