<!-- worldline-map-schema=1 -->
<!-- boundary=official-b173-public-block-lifecycle-provider -->
<!-- nonclaims=loaded-container-spill,wrong-tool-matrix,rendering,chunk-unload,process-restart -->
<!-- frozen-trace=a2541a94cd70363267c953f38abf479551440ddab60bc3e3b33c90cdec828046 -->

# Official block lifecycle provider mapping

This integration cycle exposes three exact official Beta 1.7.3 block lifecycles through the
public TestKit provider `b1.7.3-server-lifecycle`. Each row owns a fresh server workspace and
executes placement, save plus fresh login, tool-qualified break, drop observation, and a second
save plus fresh login.

Cobblestone (`block/004`) and dirt (`block/003`) route their exact drop cells through the
`ARCHETYPE` layer. Empty chest (`block/054`) routes its exact empty-container drop through the
`SINGULAR` layer. Placement, persistence, and break-to-air remain `UNIVERSAL` for all three.

The empty-chest row does not reproduce the loaded chest spill from M592. The cycle also excludes
wrong-tool rejection, randomized drop distributions, GUI behavior, rendering, chunk unload,
process restart, and mod loading. `breakTicks` and `observationTicks` are bounded windows, not
hardness or latency claims.

Frozen signal:
`provider=b1.7.3-server-lifecycle,rows=3,passed=3,layers=U-U-U-A+U-U-U-A+U-U-U-S,reload=FRESH_LOGINx6,evidence=891bfe5f4d0aae1a4fa53b93bdee0d20c3b5e7c4bf85431b27b671895bdd069d,isolation=3-fresh-worlds`.
