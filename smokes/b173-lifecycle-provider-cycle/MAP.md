<!-- worldline-map-schema=1 -->
<!-- boundary=official-b173-public-block-lifecycle-provider -->
<!-- nonclaims=loaded-container-spill,wrong-tool-matrix,rendering,chunk-unload,process-restart -->
<!-- frozen-trace=c48f1902ca6b9643a7897209a938362dfd7997c8f797abf47239b061b9a5cb23 -->

# Official block lifecycle provider mapping

This integration cycle exposes twenty-one exact official Beta 1.7.3 block lifecycles through the
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
flat stone support. Placement, persistence, and break-to-air remain `UNIVERSAL` for all
twenty-one.

The empty-chest row does not reproduce the loaded chest spill from M592. The cycle also excludes
wrong-tool rejection, randomized drop distributions, pressure activation and release, GUI
behavior, rendering, chunk unload, process restart, and mod loading. `breakTicks` and
`observationTicks` are bounded windows, not hardness or latency claims.

Frozen signal:
`provider=b1.7.3-server-lifecycle,rows=21,passed=21,layers=U-U-U-A+U-U-U-A+U-U-U-S+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A,reload=FRESH_LOGINx42,evidence=66b386c47b52c701153bfdc342d5472e5482d9d6003fc9826c34e70f29ada0a4,isolation=21-fresh-worlds`.
