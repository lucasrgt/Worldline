# M767-MODLOADER-FORGE-TESTKIT-PROVIDER ModLoader and Forge TestKit providers

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M767 adds `modloader-b1.7.3` and `forge-b1.7.3` as real TestKit runtime families. Each test receives a fresh hash-pinned single-player client, and a localhost control channel advances exactly one official game tick per TestKit command while exposing only lifecycle, world time, player identity, health, selected slot, and pose. Each session seals a profiler artifact and exits naturally. Broader gameplay mutation and performance equivalence remain unclaimed.

## Qualification cycle

`M767ModloaderForgeTestkitProviderCycle` verifies the official client and historical loader hashes, prepares isolated Java 8 RetroMCP clients, compiles and discovers both providers through Java SPI, and runs two fresh TestKit cases per loader. Qualification requires four distinct processes, four controlled ticks, four checksum-valid profiler artifacts, and four clean shutdowns. Required local inputs are supplied through `WORLDLINE_LEGACY_BASE_WORKSPACE`, `WORLDLINE_LEGACY_LOADER_ARTIFACTS`, and `WORLDLINE_JAVA8_HOME`.

Expected signal: `providers=modloader-b1.7.3+forge-b1.7.3,discovery=spi,sessions=4,testkit=4-pass,ticks=4,isolation=fresh-singleplayer-client,profiler=4-sealed-wlpr,shutdown=clean`.

Frozen semantic SHA-256: `d658516d6de1ee1fbbf97275b6ddfe893ea094dff34fb5812437ec46593d1dd9`.
