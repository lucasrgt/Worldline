<!-- worldline-map-schema=1 -->
<!-- boundary=m267-milk-bucket -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=08a4bde6b39728f2585676409fd066a93aea351c0b561981b8e29c9fc7a2cff8 -->

# M267 behavior map

Official milk bucket item `335` is used with Packet15 air-use (direction
`255` at `-1,255,-1`) while looking at a raised stone basin floor. Vanilla
`ItemBucket` with fill `-1` replaces the held stack with empty bucket `325`
when the look raytrace hits a tile. Health stays `20 -> 20`. Beta 1.7.3 has
no status effects; milk does not heal. The basin cell stays air.

This is not water pickup (M168) or lava pickup (M181).

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone-basin+milk335|cause=packet15-dir255-item335|wire=packet103-bucket325|oracle=itembucket-milk-empty-no-heal+fresh-login|column=17,floor=4:71:4:1:0,health=20->20,heal=0,held=335:1:0->325:1:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`08a4bde6b39728f2585676409fd066a93aea351c0b561981b8e29c9fc7a2cff8`.
