<!-- worldline-map-schema=1 -->
<!-- boundary=b173-wooden-door-state-domain-cycle -->
<!-- nonclaims=iron-door-redstone-destruction-collision-render -->
<!-- frozen-trace=0000000000000000000000000000000000000000000000000000000000000000 -->

# Beta 1.7.3 wooden-door state domain

This package binds the reusable public `BlockStateDomainPlan`, fixture, evidence, and official
server provider to wooden door block `64`.

Four gameplay placements consume item `324` and produce the closed directional pairs:

| Look yaw | Lower | Upper |
| --- | --- | --- |
| `-90` | `64:0` | `64:8` |
| `0` | `64:1` | `64:9` |
| `90` | `64:2` | `64:10` |
| `180` | `64:3` | `64:11` |

After the selected stack is proven empty, four gameplay activations produce lower states
`64:4` through `64:7` and upper states `64:12` through `64:15`. The final eight-cell open grid
survives a clean save and fresh login. The canonical domain is therefore every metadata value
`0..15` observed through causal actions, not sixteen independent milestones.

This map does not claim iron doors, redstone-driven activation, support destruction, collision,
sound, client rendering, or states created by direct world mutation.

Frozen signal:
`provider=b1.7.3-server-state-domain,family=wooden-door,rows=1,passed=1,states=16,reload=FRESH_LOGINx1,evidence=0000000000000000000000000000000000000000000000000000000000000000,isolation=1-fresh-worlds`.
