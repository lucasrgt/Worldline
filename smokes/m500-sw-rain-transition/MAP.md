<!-- worldline-map-schema=1 -->
<!-- boundary=state-world-differential -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=3a90b1745c24f4ba910f209f1f4939d631063e67acfcee4f610933d55b69eb7d -->

# M500-SW behavior map

Official server symbols:

- Protocol-14 packet ID `70` is `Packet70Bed`. Its payload is exactly one
  signed byte and `getPacketSize()` is `1`. Reasons: `0` missing-bed message,
  `1` begin rain, `2` end rain. Beta 1.7.3 has no reason `3`.
- `net.minecraft.src.WorldServer.updateWeather` compares rain visibility before
  and after `super.updateWeather()`. A dry-to-rain transition broadcasts a new
  `Packet70Bed(1)`; a rain-to-dry transition broadcasts reason `2`.
- `net.minecraft.src.ServerConfigurationManager` sends `Packet70Bed(1)` on
  login only when the world is already raining. The smoke arms after a
  synchronized dry login and rejects any pre-arm rain start, proving a live
  transition rather than bootstrap.
- Packet ID `71` is `Packet71Weather`, a lightning entity whose payload is
  exactly 17 bytes: `int entityId`, `byte type`, `int x`, `int y`, `int z`.
- `net.minecraft.src.WorldInfo` persists `rainTime` int, `raining` byte,
  `thunderTime` int, and `thundering` byte in the gzip-NBT `level.dat`.

Save-order oracle:

The official dedicated server saves the Overworld and the secondary dimension
sequentially through the same save handler. The later secondary-dimension write
becomes the canonical `world/level.dat` and moves the Overworld write to
`world/level.dat_old`. After a clean save/stop the smoke therefore asserts the
dual snapshot: `level.dat_old` carries the Overworld post-transition state
(`raining=true`, `thundering=false`, a freshly seeded positive `rainTime` in
`12000..23999`, and preserved seed/spawn identity), while the canonical
`level.dat` keeps the original dry state with the patched `rainTime` and
preserved identity. Canonical restart rain persistence is explicitly NOT
claimed; the live post-arm `Packet70Bed(1)` observation is the rain-start
evidence.

The qualification cycle repeats two fresh smoke scenarios. Each scenario
starts one official server to create the world and a second official server to
load the patched countdown and emit the transition, for four official server
JVMs and two protocol-14 client sessions in total.

The frozen signal names the live transition `live=packet70-reason1` and the
snapshot pair `old-snapshot=raining` / `canonical=dry-original-countdown` with
`save-order=overworld-then-secondary`. This map does not claim rain stop,
thunder/lightning behavior beyond correct Packet71 consumption, snow/ice/fire
effects, client rain particles, biomes, Nether weather, commands, a generic
weather API, or timing/performance.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=50020240820|profile=overworld|entry=patch-level-dat-dry-rainTime-400+thunder-off|fixture=overworld-dry-countdown|cause=worldserver-updateweather-dry-to-rain|wire=packet70-reason1-begin-rain|oracle=dry-before-raining-after|dimension=0,live=packet70-reason1,old-snapshot=raining,canonical=dry-original-countdown,save-order=overworld-then-secondary,thundering=false,identity=seed-spawn-preserved,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`3a90b1745c24f4ba910f209f1f4939d631063e67acfcee4f610933d55b69eb7d`.
