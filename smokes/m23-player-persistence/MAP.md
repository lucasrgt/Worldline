<!-- worldline-map-schema=1 -->
<!-- boundary=player-persistence -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=cce8512d97119d2c7fd010110a1760bebe7d86bed4f3d8cc1fefe39e58fb8928 -->

# M23 Persisted Multiplayer Player Boundary

Two fresh protocol-14 clients login to two fresh official servers, appear in
the native player list, disconnect, and disappear. Each server then forces a
save and the b1.7.3 adapter reads `world/players/Worldline.dat` through original
gzip/NBT code.

The gate requires dimension 0, health 20, an empty inventory, and a finite
position above the void. Exact spawn coordinates and entity IDs remain
observational. The official server JAR and generated player/world data remain
ignored.

Frozen expected signature SHA-256: `cce8512d97119d2c7fd010110a1760bebe7d86bed4f3d8cc1fefe39e58fb8928`
