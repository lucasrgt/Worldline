# Functional Gameplay Census

The registry census answers which b1.7.3 objects exist. The Functional Gameplay Census answers
which finite behavior claims Worldline can execute and prove for those objects. It is the progress
denominator for gameplay coverage; smoke count, milestone number, mapping identity coverage, and
Atlas matrix occupancy are not substitutes for it.

## Units

- A milestone is one coherent mini-subsystem or user-visible capability package.
- An atom is one claim with subject, operation, cause, context, and observable outcome.
- A case is one parametrized execution of an atom.
- An observation is evidence emitted by a case.

One milestone may therefore close many atoms and generate hundreds of cases. A matrix cell, block
drop, metadata value, or edge condition is not independently promoted to a milestone.

## Block denominator

The first census family expands 96 registered b1.7.3 blocks over 11 behavior dimensions, producing
1,056 candidate claims. The registry-presence template is backed for all 96 subjects by the
official-client `census-cycle`. The first curated evidence import resolves 28 additional claims for
gravity, transparent-block lighting, plant growth and harvest, sponge/glass/ice placement and
persistence, fluid flow and reaction, and furnace ticking. The first lifecycle package adds exact
placement, break, drop, and reload claims for cobblestone, dirt, logs, and chests. Together these
imports established 140 verified claims. A second curated placeable-block package imports 68
placement and persistence claims across 35 additional subjects. The census now contains 208
verified claims. A tool-qualified break/drop package adds 24 exact claims across 12 subjects. The
census then contained 232 verified claims. A singular-lifecycle package adds 23 exact claims for
pistons, dispenser, note block, bed, doors, and jukebox. The census then contained 255 verified
claims. The expanded public solid-lifecycle matrix adds exact break and drop claims for stone,
planks, sandstone, and brick. The scenario-selected mineral lifecycle matrix then adds 12 exact
claims across iron ore, diamond ore, and lapis, gold, iron, and diamond storage blocks. The census
now contains 275 verified claims (26.04%) and leaves 781 unknown. Existing smoke evidence is
reusable input, but it is never promoted automatically: its subject, action, context, observable,
oracle, signature, and automation surface must be curated first.

The singular package remains scoped to its tested cells. Regular-piston evidence breaks an
extended base; sticky-piston evidence breaks an extended head; wooden-door evidence breaks the
upper half of one closed orientation. Occupied-jukebox evidence proves persisted removal and
record spill, but does not assert a jukebox-item drop. These claims do not imply exhaustive
orientation, tool, open-state, container-content, power, or motion matrices.

The behavior dimensions are registry presence, reachable state domain, gameplay placement, break
transition, drop matrix, save/reload, collision shape, light behavior, tick policy, neighbor
response, and native rendering. Profiles route the matrix through overlapping archetypes. Subjects
whose mechanics cannot be described safely by their archetypes route archetype cases to the
singular layer. Per-template overrides remain available for mixed subjects.

## Status and automation

Functional status is one of `VERIFIED`, `PARTIAL`, `UNKNOWN`, `NATIVE_NONDETERMINISTIC`,
`NOT_APPLICABLE`, or `RETRACTED`. A verified claim requires a matching qualified evidence
signature. A non-applicable, native-nondeterministic, or retracted disposition requires an explicit
evidence-backed exception; unresolved applicability stays in the denominator.

Automation surface is tracked separately as `PUBLIC_TESTKIT`, `INTERNAL_API`, `SMOKE_ONLY`, or
`NONE`. This prevents an internal smoke from being reported as a public Cypress-like TestKit
capability.

Two percentages must remain distinct:

```text
functional proof = VERIFIED / candidate claims
census resolution = resolved claims / candidate claims
```

The target is 98.8% functional proof, not 98.8% file or mapping coverage. With the 1,056-claim
denominator, that requires 1,044 verified claims. After the earlier lifecycle imports, 789
additional verified claims were required; the solid and mineral public matrices reduce that gap to
769. The executable check prints the exact current proof, unknown count, and claims remaining to
the target on every canonical Gate.

## Three conformance layers

The public `BlockConformancePlan` expands versioned block profiles and templates deterministically:

1. Universal cases apply the same law across the registry where the template is universal.
2. Archetype cases reuse behavior by traits such as solid, gravity, fluid, vegetation, rail,
   redstone, directional, transparent, luminous, or stateful metadata.
3. Singular cases handle mechanics such as piston, bed, portal, furnace, chest, dispenser,
   jukebox, note block, TNT, sponge, fire, spawner, and cake.

`BlockLifecycleFixture` executes four routed claims as one capability. Through the orthogonal
`BlockLifecycleDriver`, it requires a gameplay placement and inventory effect, a save plus fresh
login, an exact break and tool effect, normalized zero/one/multiple drops, and a second reload that
proves the removed state persisted. The b1.7.3 server adapter implements this boundary without
turning direct `GameWorld.setBlock` mutation into false gameplay evidence. Historical claims remain
`INTERNAL_API` or `SMOKE_ONLY` until an official run binds their evidence to this public fixture.
Its current official provider package executes 16 scenario-routed rows and backs 64 public claims.
The mineral package reuses `ore` and `mineral-storage` archetypes while keeping obsidian's
distinct harvest profile explicit.

The data under `behavior/functional-census/b1.7.3/` is the versioned routing source. The Gate rejects
a missing registry subject, unprofiled block, duplicate claim, unsupported status, unqualified
verified signature, unsupported exception, or denominator drift.
