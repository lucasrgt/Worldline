# M659-POWERED-CREEPER powered creeper

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M659 freezes the official Beta 1.7.3 transformation in which an explicitly unpowered creeper is struck by an observed native lightning entity at the same cell and the same creeper identity becomes powered. The powered state must remain present after one observation tick; explosion behavior is not claimed.

## Qualification cycle

Run compares two fresh mapped executions with two direct official-server oracle executions. Each in-memory world joins one creeper, records its unpowered prerequisite and identity, joins a native lightning entity at the creeper cell, observes both entities and the live strike, invokes the native lightning callback, and records the same creeper as powered immediately and after one runtime tick. Equatable evidence normalizes process-local entity numbers into explicit identity-preservation and same-cell facts.

Expected signal: `oracle=MATCH,fixture=unpowered-creeper-observed-lightning,powered=same-identity,held=one-tick,explosion=unclaimed`.

Frozen semantic SHA-256: `a8d0b1a2e0ef160405005b422d0e4d7fce5c1ba11da765bd09f9017a1914539e`.
