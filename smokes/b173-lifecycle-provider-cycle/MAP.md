<!-- worldline-map-schema=1 -->
<!-- boundary=official-b173-public-block-lifecycle-provider -->
<!-- nonclaims=loaded-container-spill,wrong-tool-matrix,rendering,chunk-unload,process-restart -->
<!-- frozen-trace=f6b74583ba0447274583f056c1102b872c83277c565879438bf0317348c1542e -->

# Official block lifecycle provider mapping

This integration cycle exposes sixteen exact official Beta 1.7.3 block lifecycles through the
public TestKit provider `b1.7.3-server-lifecycle`. Each row owns a fresh server workspace and
executes placement, save plus fresh login, tool-qualified break, drop observation, and a second
save plus fresh login.

Cobblestone (`block/004`), dirt (`block/003`), stone (`block/001`), planks (`block/005`),
sandstone (`block/024`), and brick (`block/045`) route exact drop cells through the `ARCHETYPE`
layer. Empty chest (`block/054`) routes its exact empty-container drop through the `SINGULAR`
layer. Gold, iron, coal, and diamond ores (`block/014`, `/015`, `/016`, `/056`), lapis, gold,
iron, and diamond storage blocks (`block/022`, `/041`, `/042`, `/057`), and obsidian
(`block/049`) add nine scenario-selected block/tool rows. Placement, persistence, and
break-to-air remain `UNIVERSAL` for all sixteen.

The empty-chest row does not reproduce the loaded chest spill from M592. The cycle also excludes
wrong-tool rejection, randomized drop distributions, GUI behavior, rendering, chunk unload,
process restart, and mod loading. `breakTicks` and `observationTicks` are bounded windows, not
hardness or latency claims.

Frozen signal:
`provider=b1.7.3-server-lifecycle,rows=16,passed=16,layers=U-U-U-A+U-U-U-A+U-U-U-S+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A,reload=FRESH_LOGINx32,evidence=0d6d9f09521f77135a0b6e487199c7a9f8e5c52a84ae09126465d48fbc200db8,isolation=16-fresh-worlds`.
