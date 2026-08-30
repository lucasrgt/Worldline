# M305 qualification cycle

`PlantGrowthCycle` rebuilds the raised dirt, still-water, and isolated-sand
fixture in two fresh official server JVMs. Each run bonemeal-jumps wheat
`59:0→59:7`, waits a bounded random-tick window until cactus `81` and sugar
cane `83` both reach height `>= 2`, and reloads the same cells. One official
EOF is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`cad16d48f0b3fac39820f9055cb39978c6307c7f7be9af052d3247f9728f25bd`.

Run directly with:

```text
java tools/harness/Gate.java --milestone m305-plant-growth
```

Canonical evidence uses two official server JVMs and four client sessions.
