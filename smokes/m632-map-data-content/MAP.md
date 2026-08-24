<!-- worldline-map-schema=1 -->
<!-- boundary=map-data-content -->
<!-- nonclaims=client-rendering,map-gui,dynamic-player-markers,arbitrary-seeds-or-positions,map-zoom,nether-maps,post-beta-packet-formats -->
<!-- frozen-trace=8b54a90b94fdb4791b5892aa89c2d55b3a01fb858e24bdbc398c117ba016d373 -->

# M632 protocol-14 map data content behavior map

The official Beta 1.7.3 dedicated server first creates world seed `17320110707` and stops cleanly.
The fallback map center follows `level.dat` spawn, whose initial search is not stable across fresh
server boots, so the controlled fixture changes only `SpawnX/Y/Z` to `4:60:4` while stopped. A
fresh official observation JVM then starts a loader at that position. The loader receives all 49
chunks from the fixed view-distance-3 server and settles them for 200 ticks. A second player
persisted with first-map item `358:0` connects at the same pinned spawn. Packet131 capture begins
immediately after its handshake and before play synchronization; selecting the map drives the
qualified observation without losing any initial column.

Packet131 payload kind `0` carries a column, starting row, and a bounded run of color bytes;
payload kind `1` carries map markers. The adapter applies every color span to a 128x128 grid,
requires all 128 columns, and waits for 160 ticks without another color span. The frozen digest
therefore describes converged server-observable map colors at the fixed seed and position rather
than packet timing or the order of redundant spans.

Frozen signal:

```text
seed=17320110707,pos=4:60:4,map=358:0,columns=128,nonzero=697,palette=12,sha256=a09f5f7e8363e81f1258ee53714538b9513fc93e4255d31c41974c7b1297f956,clients=2,disconnect=clean
```

This boundary does not claim client rendering, map GUI appearance, dynamic player-marker
positions, other seeds or positions, map zoom, Nether maps, or post-Beta packet formats.

Frozen trace SHA-256: `8b54a90b94fdb4791b5892aa89c2d55b3a01fb858e24bdbc398c117ba016d373`.
