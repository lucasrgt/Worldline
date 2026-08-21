# M393 qualification cycle

`StairFacingSetCycle` rebuilds the raised stone row in two fresh official
server JVMs. Each run places oak stairs `53` with look yaw `-90` and `90`,
then cobble stairs `67` with the same two yaws, and reloads `53:0`, `53:1`,
`67:0`, and `67:1`. The signal must include both `53` and `67` plus multiple
facing metas. One official EOF is retried after a 5 second sleep. Headless
`B173WireClient` is the only client. There is no GUI and no Aero path.

Run directly with:

```text
java tools/smoke/StairFacingSetCycle.java m393-stair-facing-set
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`1e94922033cceeec477b29842f80b9bce86737cb240b266bad8ad4cf93cf0253`.
