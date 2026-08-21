# M18 Runtime Coverage Contract

## Scope

M18 closes the loop between the static semantic catalog and dynamic
execution: any public-grammar scenario can now report which control
categories of the closed 25-category catalog its steps engage, and which
catalog roles its executed trace fields carry. The check is fully neutral -
no Minecraft, adapter, or runtime classes are required.

## Command

```text
worldline coverage <scenario.wlscenario> [trace.wltrace] [min-percent]
```

- The scenario must be DSL-valid; every step maps to exactly one catalog
  category (`tick`, `tap` to input, `reseed` to rng, `block` to world,
  `observe` to lab).
- With a trace (for example produced by `scenario run`), observed schema
  fields are resolved through the closed `SemanticFields` alias table into
  catalog roles; unknown fields invent nothing.
- The optional floor percentage fails the command with exit status 3 when
  engaged categories fall below it - usable as a pre-push richness gate for
  scenario corpora.

Outputs are create-new canonical artifacts: stdout summary plus a
checksum-protected `WORLDLINE-COVERAGE/1` document (`.wlcover`, suffixed
`.traced` when a trace was provided).

## Report

The report binds scenario provenance, touched categories in canonical
catalog order with per-category step counts, the engaged percentage over the
25 total categories, and observed role names. Body checksum matches the
established repository format.

## Evidence

The m18 smoke covers a six-step scenario exercising all five mappable verbs
(rng/input/tick/world/lab = 20% of the catalog), extracts `BLOCK_ID_READ`
from an executed trace, verifies `.wlcover` artifact creation, and checks
both threshold outcomes. Frozen SHA-256 lives in
`smokes/m18-coverage/smoke.properties`.

## Non-claims

M18 does not claim role-level attribution inside a category (a `block:` step
proves world engagement, not which world role), coverage of categories that
scenario steps cannot reach (rendering, audio, persistence, network, save,
and so on), mod-callback instrumentation, or cross-scenario aggregation.
