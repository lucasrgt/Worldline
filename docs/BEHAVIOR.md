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

TestKit consumes the same evidence type instead of maintaining a second
milestone catalog:

```java
expect(modEvidence).toMatchVanilla(
        WorldlineBehavior.VOID_DEATH,
        frozenSignal,
        frozenSignature);
```

The assertion ignores the provenance lane (`vanilla` versus `mod`) but fails
when the semantic behavior or frozen signature diverges. Milestone
qualification verifies that every Atlas-backed descriptor can reach this
TestKit surface before the official cycle runs.
