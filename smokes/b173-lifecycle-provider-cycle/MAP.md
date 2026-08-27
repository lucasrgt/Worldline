<!-- worldline-map-schema=1 -->
<!-- boundary=official-b173-public-block-lifecycle-provider -->
<!-- nonclaims=loaded-container-spill,wrong-tool-matrix,rendering,chunk-unload,process-restart -->
<!-- frozen-trace=d624df7bad3b02f061aa4196818971fc1ccb959304ed418e3e4da8d172a2f257 -->

# Official block lifecycle provider mapping

This integration cycle exposes nineteen exact official Beta 1.7.3 block lifecycles through the
public TestKit provider `b1.7.3-server-lifecycle`. Each row owns a fresh server workspace and
executes placement, save plus fresh login, tool-qualified break, drop observation, and a second
save plus fresh login.

Cobblestone (`block/004`), dirt (`block/003`), stone (`block/001`), planks (`block/005`),
sandstone (`block/024`), and brick (`block/045`) route exact drop cells through the `ARCHETYPE`
layer. Empty chest (`block/054`) routes its exact empty-container drop through the `SINGULAR`
layer. Gold, iron, coal, and diamond ores (`block/014`, `/015`, `/016`, `/056`), lapis, gold,
iron, and diamond storage blocks (`block/022`, `/041`, `/042`, `/057`), and obsidian
(`block/049`) add nine scenario-selected block/tool rows. Rail, powered rail, and detector rail
(`block/066`, `/027`, `/028`) add one complete `rail` archetype family. Placement,
persistence, and break-to-air remain `UNIVERSAL` for all nineteen.

The empty-chest row does not reproduce the loaded chest spill from M592. The cycle also excludes
wrong-tool rejection, randomized drop distributions, GUI behavior, rendering, chunk unload,
process restart, and mod loading. `breakTicks` and `observationTicks` are bounded windows, not
hardness or latency claims.

Frozen signal:
`provider=b1.7.3-server-lifecycle,rows=19,passed=19,layers=U-U-U-A+U-U-U-A+U-U-U-S+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A,reload=FRESH_LOGINx38,evidence=7bb9b759c557ed21ea22118a4e6dbe6c4e1ef5e8d65cefd2e65ecefa972dd1f2,isolation=19-fresh-worlds`.
