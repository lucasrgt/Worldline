# B173-ENTITY-PERSISTENCE-ENVELOPE-CYCLE official Beta 1.7.3 entity persistence-envelope subsystem

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

One public TestKit subsystem proves native NBT serialization and EntityList reconstruction for all twenty-three concrete registered entities, preserving exact runtime type, common physical and lifecycle state, and the complete entity-specific payload. The abstract registered EntityLiving class remains an explicit non-applicable boundary.

## Qualification cycle

EntityPersistenceReplay executes two fresh mapped official-client captures. Each pass initializes native registries, seeds deterministic common and entity-specific state, writes native NBT, reconstructs through EntityList, reserializes the result, and requires type-exact, state-exact, byte-exact, deterministic public evidence.

Expected signal: `family=entity-persistence-envelope,subjects=23,claims=23,layers=UNIVERSALx23,abstract=entity/048:NOT_APPLICABLE,reconstructed=23,type-exact=23,nbt-exact=23,deterministic=true,evidence=223613ec6363b778024893808b4b52e023150f74b2e702d92abaafaffe4ddb9f`.

Frozen semantic SHA-256: `6b561fdb5e98d949487ca7629a0ad93a794bb8f214edd386252ffec065797994`.
