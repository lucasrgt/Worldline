# M122 qualification cycle

`FixedSeedRegionLightingCycle` verifies the official server artifact, compiles
the published API, adapter and smoke, and creates two fresh replicas. Each
replica uses one official server/client pair for loading and settling and a
second official server/client pair after clean restart for the final Packet51
light census.

The replicas must match both 294,912-sample plane hashes, every histogram bin,
the complete semantic trace and frozen signature. Pending or diagnostic
descriptors cannot qualify. Evidence records four official server JVMs and
four client sessions.

The frozen semantic SHA-256 is
`55f946b28a62caf43a7b02b027f13747f5662e315fbf0c8e70f9cca77a189192`.
