# M589-LIGHTNING-FIRE-SET lightning fire set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M589 freezes the official Beta 1.7.3 lightning ignition boundary without waiting for the one-in-100000 weather scheduler. Equivalent mapped and official stone-floor worlds construct the native lightning entity at a supported air cell. Normal difficulty must create fire immediately; easy difficulty must leave the same cell empty.

## Qualification cycle

Run compares two mapped and two official executions of the normal-versus-easy fixture. Each process preloads the bounded memory world, constructs the native lightning entity, joins and ticks it twice, and records only deterministic public state. Entity RNG is seeded after construction and randomized neighboring ignition is excluded from the trace.

Expected signal: `oracle=MATCH,fixture=m589-lightning-fire-set,ticks=2,controlled=true`.

Frozen semantic SHA-256: `b099beb97a56923bfc2c3f421ad099ad804b35c075c08e0421415f639896b0d1`.
