<!-- worldline-map-schema=1 -->
<!-- boundary=official-b173-static-self-drop-lifecycle-family -->
<!-- nonclaims=sponge-fluid-reaction,wool-metadata-variants,tnt-activation,fence-collision,netherrack-fire -->
<!-- frozen-trace=5958071ed109126eebc872b221826a924256b9f7e5bf759f02ab5de171bf34a4 -->

# Official static self-drop lifecycle family mapping

This independent family routes five metadata-zero rows through the reusable public lifecycle
harness. Sponge (`block/019`) and TNT (`block/046`) retain `SINGULAR` drop routing because their
other behaviors are subject-specific. White wool (`block/035`), fence (`block/085`), and
netherrack (`block/087`) route exact one-item self drops through `ARCHETYPE`. Placement,
fresh-login persistence, break-to-air, and the second fresh-login boundary are `UNIVERSAL`.
The exact tool-state oracle retains undamaged shears after the placed-wool break and requires one
damage on the selected pickaxe or axe for the other four rows.

The family does not claim sponge water absorption, dyed wool variants, TNT ignition/explosion,
fence collision/connectivity, or netherrack fire persistence. It also excludes wrong-tool
matrices, rendering, chunk unload, process restart, and mod loading.

Frozen signal:
`provider=b1.7.3-server-lifecycle,family=static-self-drop,rows=5,passed=5,layers=U-U-U-S+U-U-U-A+U-U-U-S+U-U-U-A+U-U-U-A,reload=FRESH_LOGINx10,evidence=fd6a738b9c1fbdab1ce981d6e70a5be46014e28644b2be8240b3d91f59391a5b,isolation=5-fresh-worlds`.
