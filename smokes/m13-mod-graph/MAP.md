# M13 Multi-Mod Graph Smoke Map

## Objective

Prove deterministic multi-mod ordering over format 2 descriptors: topological
resolution with lexicographic tie-breaking, input-order independence, and
fail-closed rejection of missing dependencies, unmet minimum versions, self
dependencies, and dependency cycles.

## Oracle

The neutral `worldline.mods` module is exercised directly in two fresh
processes; both must print the same resolved order and rejection set. No
Minecraft runtime is involved; the descriptor grammar itself is covered by the
module unit suite.

## Mappings

- `ModGraph.order` consumes inspected compatible artifacts only.
- Dependency tokens use the canonical `id` / `id>=x.y.z` spellings parsed by
  `ModDependency`.

## Exclusions

- No version-range expressions beyond a single minimum version.
- No runtime classpath isolation between mods beyond the existing per-JAR
  loaders; ordering evidence only claims callback sequence.
- Simultaneous multi-mod execution inside the controlled client is exercised
  by `B173Runtime.installMods` and remains subject to later differential
  coverage; this smoke freezes resolution semantics.

## Pass conditions

- app -> core -> lib resolves to lib, core, app regardless of input order.
- missing, version shortfall, self, and cycle cases all throw.
- Frozen evidence SHA-256 matches smoke.properties.

Frozen expected signature SHA-256: `353a640be5bf2a77cdfd921c10b0525b462839e2f2d2591afb99223360bd67e5`
