# M597-FALLING-SAND-ENTITY-SET falling sand entity set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

Unsupported sand becomes a Packet23 falling-sand entity, then places as sand 12:0 when it lands. This SET proves the observed entity type plus the persisted landed block. It is distinct from M119's block-only settle, M274 gravel, and M342's sand-plus-gravel pair.

## Qualification cycle

DataDrivenCycle rebuilds the supported-sand fixture in two fresh official server JVMs. Each run removes the stone support, records the live Packet23 object type from the official oracle, waits for sand 12:0 to land, and reloads that cell after save plus fresh login. One official EOF is retried after a 5 second sleep.

Expected signal: `column=10,lower=4:64:4:1:0->12:0,upper=4:65:4:12:0->0:0,entity-type=70,packet23=70,persisted=true,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `cc5127dcadf21010c8d1a840f832d3e1b95b803bd2b8d74dd7a5c77984a7328b`.
