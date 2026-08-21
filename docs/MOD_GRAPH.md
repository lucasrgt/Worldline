# M13 Multi-Mod Graph Contract

## Scope

M13 removes the single-mod limitation at the resolution layer. Format 2
descriptors may declare dependencies; `worldline.mods.ModGraph` resolves a
deterministic load order over inspected, compatible artifacts and fails
closed on every unsatisfiable input.

## Descriptor format 2

Format 2 keeps the six format 1 fields in order and appends one field:

```text
format=2
id=worldline.app
version=0.3.0
entrypoint=worldline.benchmark.AppMod
worldline.api=1
runtime=b1.7.3
requires=worldline.core,worldline.lib>=1.0.0
```

The `requires=` value is a comma-separated list of canonical tokens: a mod id,
optionally followed by `>=` and an exact semantic version minimum. An empty
value declares no dependencies. Format 1 packages remain valid and dependency
free; unknown formats, reordered fields, duplicate dependencies, malformed
ids, and malformed versions are rejected.

## Resolution

`ModGraph.order(List<ModArtifact>)` returns every artifact exactly once such
that each dependency precedes its dependent:

- duplicate ids, self dependencies, unknown dependencies, unmet minimums, and
  cycles throw;
- ties break lexicographically by mod id, so the order depends only on the
  artifact set, never on the input order;
- versions compare numerically per component; a pre-release is lower than the
  same release.

## Execution order

Adapters consume the resolved list directly
(`B173Runtime.installMods`): callbacks run in resolved order per tick and
disposal runs in reverse. The graph module stays game independent.

## Evidence

The m13 smoke resolves a three-mod chain from both input orders to one order,
rejects missing, version-shortfall, self, and cycle cases, and freezes the
result hash in `smokes/m13-mod-graph/smoke.properties`.

## Non-claims

M13 does not claim version ranges beyond a single minimum, optional
dependencies, circular-tolerance strategies, per-mod permissions, classloader
namespacing between mods, or hot reload. Simultaneous multi-mod execution
inside the controlled client inherits M11 callback semantics.
