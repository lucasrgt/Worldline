<!-- worldline-map-schema=1 -->
<!-- boundary=natural-slime-spawn -->
<!-- nonclaims=swamp-spawning,arbitrary-seeds,spawn-frequency,exact-position,exact-size,combat -->
<!-- frozen-trace=3ec318921ce8e424ea5a897c436ffc45d1e69fb09a3ce71808ba3c425db3ecc2 -->

# M635 natural slime spawn behavior map

The reusable boundary is `worldline.testkit.NaturalSlimeSpawnFixture#await`. It permits at most the caller-declared attempt limit and accepts only a type-55 official spawn below y=16 whose quantized position lies in the declared matrix and independently satisfies the slime-chunk formula. The successful attempt number and exact selected chunk are excluded from equatable evidence.

The fixture surface generates and saves the 7x7 matrix with monsters disabled, solidifies its underground blocks below y=60, then carves three vertically separated 14x14x4 air rooms entirely below y=16 in each of the sixteen formula-selected chunks. The action surface restarts the official server with monsters enabled, loads the matrix from a distant observer, and waits for the ordinary spawning loop. The observation surface retains formula membership, mob type, height, matrix bounds, and qualifying-chunk count while deliberately removing entity ID, selected chunk, precise position, tick, and size randomness.

The proof uses two fresh official dedicated-server replicas and no spawner block, spawner NBT, inserted mob, mapped source, or controlled-runtime replacement.

Frozen signal: `seed=17320110707,matrix=95:101:40:46,slime-chunks=16,geometry=matrix-solid-below60+3xrooms14x14x4-under16,type=55,y<16,formula=verified,natural=no-spawner,bounded<=4800,replicas=2,disconnect=clean`.
