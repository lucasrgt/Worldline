# M305 qualification cycle

`PlantGrowthCycle` rebuilds the raised dirt, still-water, and isolated-sand
fixture in two fresh official server JVMs. Each run bonemeal-jumps wheat
`59:0→59:7`, waits a bounded random-tick window until cactus `81` and sugar
cane `83` both reach height `>= 2`, and reloads the same cells. One official
EOF is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`b755666b909da0bc4583bf752a32ff032894d3959b4dd0a47c56d3e80c066721`.

Run directly with:

```text
java tools/smoke/PlantGrowthCycle.java m305-plant-growth
```

Canonical evidence uses two official server JVMs and four client sessions.
