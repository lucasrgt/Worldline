<!-- worldline-map-schema=1 -->
<!-- boundary=b173-static-collision-envelope-cycle -->
<!-- nonclaims=dynamic-neighbor-shapes,entity-pathfinding,selection-raytrace,native-render -->
<!-- frozen-trace=0000000000000000000000000000000000000000000000000000000000000000 -->

# Beta 1.7.3 static collision envelopes

Five public TestKit rows compare identical Packet13 trajectories before and after official
gameplay placement. Stone, stone slab, wooden stairs, adjacent fence, and standing torch cover
full-cube, half-step, directional-step, raised-fence, and no-collision envelopes. Each trajectory
returns to the same origin before the next probe, and every treatment geometry survives a clean
save plus fresh login.

The package contains five Functional Census atoms and ten treatment trajectories inside one
coherent collision mini-subsystem. It does not create one milestone per block or per height.

This map does not claim dynamic neighbor-dependent shape closure, mob pathfinding, client
selection ray tracing, or native rendering.

Frozen signal:
`provider=b1.7.3-server-collision,family=static-envelope,rows=5,passed=5,probes=10,reload=FRESH_LOGINx5,evidence=0000000000000000000000000000000000000000000000000000000000000000,isolation=5-fresh-worlds`.
