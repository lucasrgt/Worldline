<!-- worldline-map-schema=1 -->
<!-- boundary=chunk-unload-reload -->
<!-- nonclaims=restart-persistence,cross-dimension,entity-id-stability -->
<!-- frozen-trace=b3f9ef8c5fcb0ea1ed9a9b3850946d8ed75466e8ef67c24e926a593bfeb42dff -->

# M627 chunk unload/reload behavior map

The official Beta 1.7.3 dedicated server runs one fixed-seed fixture outside the protected spawn
region. A lit furnace with completed glass and remaining burn time, a dropped dirt item, and an
empty minecart occupy chunk `20:20`. The operator is teleported to chunk `40:40`; protocol-14
Packet50 must evict `20:20`, and no player remains there for 100 ticks.

A fresh client then reloads the target chunk in the same server process. The furnace remains block
62 with glass 20 in its output, the item remains `3:1:0`, and the minecart remains Packet23 type
10. Item and cart IDs must rotate, proving serialized entity reconstruction rather than a second
observer of the original live objects. The reusable TestKit fixture ignores those unstable IDs in
its equatable evidence while requiring their rotation and target-chunk membership.

Frozen signal:

```text
chunk=20:20,unload=packet50,reload=fresh-client,furnace=62+glass20,item=3:1:0,minecart=type10,identity=rotated,replicas=2,disconnect=clean
```

This boundary does not claim full-server restart persistence, cross-dimension eviction, or entity
ID stability.

Frozen trace SHA-256: `b3f9ef8c5fcb0ea1ed9a9b3850946d8ed75466e8ef67c24e926a593bfeb42dff`.
