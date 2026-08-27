<!-- worldline-map-schema=1 -->
<!-- boundary=b173-cardinal-placement-state-domain-cycle -->
<!-- nonclaims=activation,inventory-content,double-chest,lit-state,render -->
<!-- frozen-trace=5f20914362da8cf0e3dc3329db1c33e6e71ded929f856d0cccc95309f15231e8 -->

# Beta 1.7.3 cardinal placement state domains

This package exercises one reusable public `BlockStateDomainPlan` family against the unmodified
official Beta 1.7.3 server. Seven caller-owned rows cover dispenser `23`, wood stairs `53`, chest
`54`, furnace `61`, cobblestone stairs `67`, pumpkin `86`, and jack-o'-lantern `91`.

Each row places four blocks at yaws `0`, `90`, `180`, and `-90` in its own fresh world, consumes
the exact four-item loadout, and proves the resulting grid again after a fresh login. The family
closes 25 distinct reachable states. Chest is the deliberate negative control: all four yaws remain
server block state `54:0`; its visual orientation is not represented by server block metadata.

This is one directional-placement subsystem proof, not seven milestones or 28 placement atoms.
It does not claim block activation, container contents, double-chest composition, lit-furnace
transition, breaking, collision, sound, or client rendering.

Frozen signal:
`provider=b1.7.3-server-state-domain,family=cardinal-placement,rows=7,passed=7,states=25,reload=FRESH_LOGINx7,evidence=efe1ac0094922b19455c887529776eba0bdee0cae93733ce196d02e8d953b994,isolation=7-fresh-worlds`.
