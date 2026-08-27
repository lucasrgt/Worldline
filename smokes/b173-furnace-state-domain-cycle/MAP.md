<!-- worldline-map-schema=1 -->
<!-- boundary=b173-furnace-state-domain-cycle -->
<!-- nonclaims=smelting,container-inventory,lit-state,render -->
<!-- frozen-trace=0000000000000000000000000000000000000000000000000000000000000000 -->

# Beta 1.7.3 furnace facing state domain

This package reuses the public `BlockStateDomainPlan`, fixture, evidence, and official server
provider for unlit furnace block `61`.

Four gameplay placements consume item `61` and produce the complete horizontal facing domain:

| Look yaw | Furnace state |
| --- | --- |
| `0` | `61:2` |
| `90` | `61:5` |
| `180` | `61:3` |
| `-90` | `61:4` |

The four-cell grid survives a clean save and fresh login. This is one complete directional
subsystem proof, not four independent milestones.

This map does not claim smelting, container inventory, the lit block id, block breaking,
collision, sound, or client rendering.

Frozen signal:
`provider=b1.7.3-server-state-domain,family=furnace,rows=1,passed=1,states=4,reload=FRESH_LOGINx1,evidence=0000000000000000000000000000000000000000000000000000000000000000,isolation=1-fresh-worlds`.
