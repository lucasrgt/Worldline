# M638-CREEPER-TNT-DIFFERENTIAL creeper versus TNT differential

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M638 qualifies the Beta 1.7.3 creeper-versus-TNT explosion-strength differential. An official proximity-fused creeper emits Packet60 strength 3. An official flint-primed TNT emits Packet60 strength 4. The normalized difference is exactly one and TNT is stronger. The claim excludes crater size, destroyed-block count, player damage, fuse duration, powered creepers, beds, and chains.

## Qualification cycle

DataDrivenCycle executes two fresh composite replicas. Each replica runs the already-qualified M391 creeper explosion scenario and M381 TNT prime scenario in independent fresh official-server workspaces, then compares their Packet60 strengths through CreeperTntDifferentialFixture. Nested evidence remains fail-closed; entity IDs, coordinates, crater RNG, and timing never enter the differential.

Expected signal: `creeper=packet60:strength3,tnt=packet60:strength4,delta=1,ordering=creeper<tnt,official-probes=2,replicas=2,disconnect=clean`.

Frozen semantic SHA-256: `66976217339dbcfb7b060a78387200c36ec9832ef648ad1802ced89cf02b033f`.
