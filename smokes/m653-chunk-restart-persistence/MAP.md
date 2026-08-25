<!-- worldline-map-schema=1 -->
<!-- boundary=chunk-restart-persistence -->
<!-- nonclaims=in-process-reload,entity-id-rotation,cross-dimension -->
<!-- frozen-trace=898bb819c0254d8ea34355180387bd7602747da87866ff5e2e91275571ca876c -->

# M653 chunk restart persistence behavior map

The official Beta 1.7.3 dedicated server runs one fixed-seed fixture outside the protected spawn
region. A chest stocked with glass, a dropped dirt item, and an empty minecart occupy chunk
`20:20`. The operator is teleported to chunk `40:40`; protocol-14 Packet50 must evict `20:20`,
and no player remains there for 100 ticks before the server persists and stops gracefully.

A brand-new dedicated-server process then boots on the same workspace and a fresh client reloads
the target chunk. The reopened chest still holds exactly the stored glass stack in its first slot,
the item remains `3:1:0`, and the minecart remains Packet23 type 10 near its rail. Entity IDs are
normalized: they are excluded from comparison and from the equatable evidence, because a new
process legitimately reassigns them, so this boundary proves serialized persistence rather than
in-memory survival.

Frozen signal:

```text
chunk=20:20,unload=packet50,stop=graceful,restart=new-process,reload=fresh-client,chest=glass20,item=3:1:0,minecart=type10,identity=normalized,replicas=2,disconnect=clean
```

This boundary does not claim in-process chunk lifecycle behavior, entity ID rotation or stability,
or cross-dimension eviction.

Frozen trace SHA-256: `898bb819c0254d8ea34355180387bd7602747da87866ff5e2e91275571ca876c`.
