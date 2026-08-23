# M598-GRAVEL-FALL-SET gravel fall set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

Unsupported gravel becomes a Packet23 falling-gravel entity, then places as gravel 13:0 when it lands, or drops as an item if that cell cannot accept the block. This SET proves the observed entity type plus the persisted landed gravel. It is distinct from M119/M597 sand type 70, M274 block-only gravel settle, and M342's sand-plus-gravel pair.

## Qualification cycle

DataDrivenCycle rebuilds the supported-gravel fixture in two fresh official server JVMs. Each run removes the stone support, records the live Packet23 object type from the official oracle, waits for gravel 13:0 to land, and reloads that cell after save plus fresh login. One official EOF is retried after a 5 second sleep.

Expected signal: `column=10,lower=4:64:4:1:0->13:0,upper=4:65:4:13:0->0:0,entity-type=71,packet23=71,persisted=true,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `18cfe17d0447d6e4b77b09df02092118df5f6b7ed58ee4697eb9882468eed37d`.
