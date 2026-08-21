# M19 HTML Evidence Contract

## Scope

M19 renders canonical trace evidence as deterministic, self-contained HTML:
a single-trace viewer and a two-trace structural diff with the first
divergence highlighted. Pages are pure functions of their inputs - no
scripts, no external assets, no timestamps - so identical inputs always
produce byte-identical documents that can be frozen like any other evidence.

## Command

```text
worldline trace html <left.wltrace> [right.wltrace] <output.html>
```

- Single mode renders provenance (seed, document SHA-256, schema) plus the
  full record table.
- Diff mode adds right-hand columns per schema field, dims equal rows,
  outlines the first divergent record, and prints an explicit EQUAL or
  DIVERGED verdict naming the first-divergence field.
- Output uses create-new semantics; exit status is 0 on success, 1 on
  invalid input, 2 on usage errors.

The renderer lives in the neutral `analysis` module (`TraceHtml`) and escapes
all variable text; pages contain no script elements by contract.

## Evidence

The m19 smoke executes two real scenarios under one seed (glass versus gold
block write), renders both page kinds through the public launcher, asserts
verdicts, highlighting markers, absence of scripts, byte-determinism of a
fresh re-render in a separate process, and freezes both page digests in
`smokes/m19-html/smoke.properties`.

## Non-claims

M19 does not claim interactive filtering, search, coverage or profile
rendering, theming, or accessibility certification beyond valid static HTML.
