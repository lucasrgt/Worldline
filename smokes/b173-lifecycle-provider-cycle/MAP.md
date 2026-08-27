<!-- worldline-map-schema=1 -->
<!-- boundary=official-b173-public-block-lifecycle-provider -->
<!-- nonclaims=loaded-container-spill,workstation-gui-and-contents,wrong-tool-matrix,rendering,chunk-unload,process-restart -->
<!-- frozen-trace=f502f9ab461fb25d4d55e1f73b6071f4c0e8d221a8d9387d13e13f86206c205c -->

# Official block lifecycle provider mapping

This integration cycle exposes twenty-six exact official Beta 1.7.3 block lifecycles through the
public TestKit provider `b1.7.3-server-lifecycle`. Each row owns a fresh server workspace and
executes placement, save plus fresh login, tool-qualified break, drop observation, and a second
save plus fresh login.

Cobblestone (`block/004`), dirt (`block/003`), stone (`block/001`), planks (`block/005`),
sandstone (`block/024`), and brick (`block/045`) route exact drop cells through the `ARCHETYPE`
layer. Empty chest (`block/054`) routes its exact empty-container drop through the `SINGULAR`
layer. Gold, iron, coal, and diamond ores (`block/014`, `/015`, `/016`, `/056`), lapis, gold,
iron, and diamond storage blocks (`block/022`, `/041`, `/042`, `/057`), and obsidian
(`block/049`) add nine scenario-selected block/tool rows. Rail, powered rail, and detector rail
(`block/066`, `/027`, `/028`) add one complete `rail` archetype family. Stone and wooden
pressure plates (`block/070`, `/072`) add the complete `pressure-plate` lifecycle family on
flat stone support. Empty dispenser (`block/023`), note block (`block/025`), crafting table
(`block/058`), furnace (`block/061`), and jukebox (`block/084`) add one complete
`empty-workstation` family. Dispenser `23:2` and furnace `61:2` preserve their official
directional placement metadata. The four singular subjects route drops through `SINGULAR`;
crafting table routes through `ARCHETYPE`. Placement, persistence, and break-to-air remain
`UNIVERSAL` for all twenty-six.

The empty-chest row does not reproduce the loaded chest spill from M592. The cycle also excludes
wrong-tool rejection, randomized drop distributions, pressure activation and release, workstation
GUI/content behavior, rendering, chunk unload, process restart, and mod loading. `breakTicks` and
`observationTicks` are bounded windows, not hardness or latency claims.

Frozen signal:
`provider=b1.7.3-server-lifecycle,rows=26,passed=26,layers=U-U-U-A+U-U-U-A+U-U-U-S+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-S+U-U-U-S+U-U-U-A+U-U-U-S+U-U-U-S,reload=FRESH_LOGINx52,evidence=bf0ae9381a09e8590778edb65f8e0819f4fb8444324d4b5e771dfdb093f99c59,isolation=26-fresh-worlds`.
