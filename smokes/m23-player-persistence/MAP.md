# M23 Persisted Multiplayer Player Boundary

Two fresh protocol-14 clients login to two fresh official servers, appear in
the native player list, disconnect, and disappear. Each server then forces a
save and the b1.7.3 adapter reads `world/players/Worldline.dat` through original
gzip/NBT code.

The gate requires dimension 0, health 20, an empty inventory, and a finite
position above the void. Exact spawn coordinates and entity IDs remain
observational. The official server JAR and generated player/world data remain
ignored.
