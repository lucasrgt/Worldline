<!-- worldline-map-schema=1 -->
<!-- boundary=native-entity-stimulus -->
<!-- nonclaims=all-damage-causes,all-loot-contexts,all-ai-paths,network-delivery,world-restarts -->
<!-- frozen-trace=a2aeebbf1d438995fc7c998a19e7a590ceda27505c7765e31b1dff287be9b5d9 -->

# Native entity stimulus semantic map

Public behavior: `native-entity-stimulus` (`WorldlineEnvironmentBehaviors.NATIVE_ENTITY_STIMULUS`).
Public acceptance: `NativeEntityStimulusFixture#verify`.

| Boundary | Native action | Paired observation |
| --- | --- | --- |
| Materialization | World.entityJoinedWorld, tracker getSpawnPacket, packet write/read | Membership changes; native identity, fixed-point position and payload survive parsing |
| Damage | Entity.attackEntityFrom, living onUpdate | Unhit, one-damage and lethal health/status/removal, including false-return item destruction and immune objects |
| Drop matrix | Eight seeded lethal native attacks | Native item entities and stack bags versus zero-yield untouched controls |
| Pickup | Entity.onCollideWithPlayer | Real inventory transfer and entity removal; delay/shake control suppresses transfer |
| Riding | Entity.interact twice | Reciprocal native mount and dismount pointers |
| Milking | Cow.interact | Empty bucket becomes milk; stone remains unchanged |
| Combat | Native melee/creeper attack and ghast action update | Cooldown gates damage, distance gates ignition, counter gates native fireball insertion |
| Body contact | Entity.applyEntityCollision | Opposing impulses versus colocated zero-impulse control |
| Snowball contact | Native onUpdate with/without joined target | Projectile removal on hit, native zero-damage target survives |
| Unsupported interaction | Native TNT/sand interact | False return preserves player health, entity position and survival |

Only original Worldline setup and reflection adapters are maintained. Existing hash-pinned Tiny mappings
resolve native members. Official classes are the behavioral oracle; no decompiled sources are copied.
The constructor-only player shell has zero declared methods and inherits native EntityPlayer behavior.
The 53 cells bind the matching four-process native capture and public fixture; stronger prior proofs remain intact.

Frozen four-process signature: `a2aeebbf1d438995fc7c998a19e7a590ceda27505c7765e31b1dff287be9b5d9`.
Required semantic signal: `family=native-entity-stimulus,subjects=20,cases=53,processes=4,oracle=mapped-official-native-state`.
Required trace: `v1|server=official-b1.7.3|family=native-entity-stimulus|subjects=20|cases=53|oracle=mapped-official-native-state`.
