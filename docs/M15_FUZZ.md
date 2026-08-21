# M15 Differential Fuzzer Contract

## Scope

M15 composes the platform's rare pieces - the public scenario DSL, the M9
minimizer, controlled subjects, and frozen evidence - into one command that
searches for behavioral disagreements automatically. Fuzzing is
deterministic: a campaign is fully described by its seed, case count, step
bound, subject set, and budget, and reruns reproduce identical reports.

## Command

```text
worldline fuzz <out-dir> <seed> <cases> <max-steps> [left.jar] [right.jar]
```

- Zero JARs hunts nondeterminism: the vanilla subject executes every candidate
  twice; any trace disagreement is reported unminimized.
- One JAR compares vanilla against the mod.
- Two JARs compare the two mods directly; every divergence is shrunk by the
  deterministic delta debugger into a minimal `.wlscenario` reproducer.

Exit status is 0 for a clean campaign, 3 when findings exist, 1 on failure,
and 2 on usage errors. Outputs are create-new: `fuzz-report.txt` plus
`finding-N.wlscenario` per finding.

## Subjects

`worldline.fuzz.FuzzSubjectProvider` binds mod JARs to named executors
reflectively (`worldline.fuzz.provider`, default
`worldline.b173.B173FuzzSubjects`). Subject labels are stable provenance
(`vanilla`, `mod:<id>@<version>`) embedded in the canonical
`WORLDLINE-FUZZ/1` report together with evaluation counts, embedded scenario
artifacts (base64url plus SHA-256), and a body checksum.

## Generation bounds

The planner emits only DSL-valid steps with bounded integers. Block writes
draw from a curated list of always-registered b1.7.3 block ids; adapters still
fail closed on unregistered ids, so an invalid candidate fails loudly instead
of silently vanishing.

## Evidence

The m15 smoke fuzzes the two M8 mod versions through the public launcher:
the first divergence arrives within the budget already minimized, validates as
a public-grammar scenario, and a vanilla-only campaign comes back clean under
the same seed. Frozen SHA-256 lives in `smokes/m15-fuzz/smoke.properties`.

## Non-claims

M15 does not claim coverage-guided generation, cross-seed flake minimization
(nondeterminism findings are reported raw because shrinking requires a
deterministic predicate), parallel campaigns, or universal semantic validity
of generated inputs beyond the documented bounds.
