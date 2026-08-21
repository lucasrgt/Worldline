# Vanilla behaviors

Public testkit identity is a semantic behavior, not a development milestone.
Smoke ids such as `m448-creeper-fuse-set` stay harness-only.

```text
WorldlineBehavior.CREEPER_FUSE
  token     = creeper-fuse
  atlasId   = atlas.scenario.creeper-fuse
  family    = hostile
WorldlineEvidence.pin(WorldlineBehavior.CREEPER_FUSE, signal, signature)
Creeper.stayUntilExplode(actor, spawn)
Creeper.evidence(signal, signature)
```

`WorldlineBehavior.require` accepts a token or Atlas id. A progress smoke id
is accepted only as an import alias and is never stored on evidence.
Equality is behavior token plus semantic SHA-256.
