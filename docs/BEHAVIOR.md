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

## Milestone completeness gate

Every new behavioral milestone must add these manifest fields before the
canonical gate accepts it:

```text
behavior=<stable-token>
testkit.fixture=<reusable-fixture>
testkit.actions=<comma-separated-actions>
testkit.observations=<comma-separated-observations>
testkit.binding=<public.class.Name#method>
testkit.evidence=equatable
```

The behavior token must exist in `WorldlineBehavior`; the binding must resolve
to one public product or adapter source method; actions and observations must
be unique stable tokens; and signal/signature evidence must already be frozen.
Milestone numbers remain import aliases only.

Tooling milestones use the same evidence and binding fields but declare
`testkit.contract=<stable-token>` from `WorldlineContract` instead of a
`behavior`. The two identities are mutually exclusive: a replay, trace,
package, or minimization contract must not be mislabeled as vanilla behavior.

The historical backlog is an explicit ratchet in
`behavior/coverage.properties`. Backfill changes must reduce
`pending.expected`; it may never increase. Pending means incomplete work, not
an accepted legacy state. New milestones cannot enter that temporary backlog.
