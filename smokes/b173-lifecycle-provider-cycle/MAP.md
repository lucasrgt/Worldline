<!-- worldline-map-schema=1 -->
<!-- boundary=official-b173-public-block-lifecycle-provider -->
<!-- nonclaims=loaded-container-spill,wrong-tool-matrix,rendering,chunk-unload,process-restart -->
<!-- frozen-trace=efa7a406eeeca4a67e0e6789227973e36c45f18a8309dd509fb21afa785abc17 -->

# Official block lifecycle provider mapping

This integration cycle exposes seven exact official Beta 1.7.3 block lifecycles through the
public TestKit provider `b1.7.3-server-lifecycle`. Each row owns a fresh server workspace and
executes placement, save plus fresh login, tool-qualified break, drop observation, and a second
save plus fresh login.

Cobblestone (`block/004`), dirt (`block/003`), stone (`block/001`), planks (`block/005`),
sandstone (`block/024`), and brick (`block/045`) route exact drop cells through the `ARCHETYPE`
layer. Empty chest (`block/054`) routes its exact empty-container drop through the `SINGULAR`
layer. Placement, persistence, and break-to-air remain `UNIVERSAL` for all seven.

The empty-chest row does not reproduce the loaded chest spill from M592. The cycle also excludes
wrong-tool rejection, randomized drop distributions, GUI behavior, rendering, chunk unload,
process restart, and mod loading. `breakTicks` and `observationTicks` are bounded windows, not
hardness or latency claims.

Frozen signal:
`provider=b1.7.3-server-lifecycle,rows=7,passed=7,layers=U-U-U-A+U-U-U-A+U-U-U-S+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A,reload=FRESH_LOGINx14,evidence=90c4d93cb72e757d6d85d70dddcc72964da087313e8d7e532dbd6536e4040e05,isolation=7-fresh-worlds`.
