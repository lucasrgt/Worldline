# M14 Scenario DSL Smoke Map

## Objective

Prove the public scenario DSL end to end through the repository launcher:
authoring with `observe`/`block`/`tick`/`reseed` verbs, strict validation,
rejection of out-of-grammar steps, and deterministic controlled execution via
the reflective scenario provider.

## Oracle

The controlled mapped client executes the authored scenario twice in fresh
processes under seed 4242; both emitted canonical traces must be byte-identical.
The client remains under the frozen controlled-client signatures.

## Mappings

- `worldline.b173.B173ScenarioRunner` maps DSL verbs onto the M3 surface:
  block writes use `GameWorld.setBlock`, observations record the client tick
  and the block column at (8,65,8).
- Validation is grammar-level only; semantic bounds are enforced by
  `ScenarioStep` construction.

## Exclusions

- The minimizer is not re-run here; M9 owns delta-debugging evidence. DSL
  scenarios are ordinary `Scenario` artifacts for the minimizer.
- Only the listed verbs are public; adapter-private vocabulary (for example
  lab/noise steps) stays outside the DSL.

## Pass conditions

- `scenario create` accepts five DSL steps.
- `scenario validate` prints typed steps with the dsl/1 version marker.
- An unknown verb fails validation with exit status 1.
- Two `scenario run` executions produce identical traces.
- Frozen evidence SHA-256 matches smoke.properties.

Frozen expected signature SHA-256: `6a2e966f444390ef60a2b09918fa4e541378f3f09e497dc49bb39466b85e57bb`
