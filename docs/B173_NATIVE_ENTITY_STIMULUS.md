# B173-NATIVE-ENTITY-STIMULUS-CYCLE native entity materialization, damage, drops and causal contacts

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

This reusable native-entity-stimulus package covers 53 bounded Census cells across 20 concrete registrations. Five spawn cases insert the native entity into a native world and round-trip its tracker-produced packet. Sixteen damage cases compare no attack, one damage and lethal damage including delayed living removal. Fifteen drop matrices compare eight seeded native lethal attacks against untouched controls. Seventeen contacts cover pickup, mounting, milking, body collision, projectile impact, melee cooldown, ignition and ghast firing, plus explicit unsupported TNT and falling-sand interaction invariants. Existing stronger gameplay proofs remain independently bound.

## Qualification cycle

NativeInventoryRenderCycle dispatches the shared NativeDifferentialCapture runner into two mapped-server and two untouched official-server processes. Every action executes original native code; constructor and field assignments establish preconditions only. A constructor-only player subclass inherits every EntityPlayer method without overrides and excludes MP transport behavior. NativeEntityStimulusFixture rejects missing or reordered controls, fabricated damage, unrelated drops and absent inventory transfers. Qualification freezes all 53 equatable observations. The bounded contracts do not claim exhaustive damage causes, all loot contexts, all AI paths, socket delivery or world restarts. The abstract EntityLiving registry row remains independently not applicable for its ten non-registry templates.

Expected signal: `family=native-entity-stimulus,subjects=20,cases=53,processes=4,oracle=mapped-official-native-state`.

Frozen semantic SHA-256: `a2aeebbf1d438995fc7c998a19e7a590ceda27505c7765e31b1dff287be9b5d9`.
