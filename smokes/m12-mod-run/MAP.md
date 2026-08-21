# M12 Attested Mod Test Run Smoke Map

## Objective

Prove the one-command `mod test run` flow end to end through the public
launcher: inspection of a format 2 package, execution inside the controlled
b1.7.3 runtime, attested `.wlmtest` output (`execution=controlled-runtime`,
`seed`, `ticks`), cross-process determinism, equality diffing, and corruption
rejection.

## Oracle

Two independent launcher processes execute the same mod JAR with the same
seed and tick budget; both emitted results must be byte-identical, and their
embedded canonical traces must carry the same SHA-256. The controlled runtime
itself remains under the M2/M3 boundary controls and the frozen client-cycle
signatures.

## Mappings

- `worldline.b173.B173ModTestRunner` inspects, loads, boots, installs, ticks,
  and records one observation per tick (`tick`, `block65`, `entityCount`).
- The CLI binds the runner reflectively via `worldline.modtest.provider`.

## Exclusions

- The result digest is intentionally excluded from the frozen evidence because
  JAR packaging embeds timestamps; trace and behavior facts are frozen instead.
- No legacy ModLoader/Forge compatibility is claimed.

## Pass conditions

- `mod inspect` reports COMPATIBLE for a format 2 descriptor.
- `mod test run` exits 0 printing execution/seed/ticks provenance.
- Two runs produce byte-identical results; `mod test diff` reports EQUAL.
- A corrupted result is rejected with exit status 1.
- Frozen evidence SHA-256 matches smoke.properties.
