# Fixed-Wait Migration

Worldline distinguishes a condition wait from an intentional temporal observation window.
`WorldlineAwait` provides bounded `awaitSlot`, `awaitBlock`, and `awaitEntity` polling. It
records waits, polls, failures, and deliberately observed game ticks. A smoke that must
observe behavior across an exact tick interval uses `WorldlineSmokeAwait.observe`; it may
not hide that interval behind a raw `sustainTicks` call immediately followed by an assertion.

`smokes/fixed-wait-migration.lock` is the content-addressed review record for the legacy
classification. It binds 226 source rewrites across 216 milestones to their old and new
fingerprints and to the previously accepted evidence. Only those milestones acquire the
`smoketest` source input. The other 309 portable qualification pins are unchanged.
For data-driven affected milestones, `cycle.inputs` gains that module explicitly; therefore
its plan hash changes even when the generic runner source and prior fingerprint do not.
The overlapping EOF-retry attestations use LF-normalized source hashes on every platform.
Their finalizer is idempotent: repeated qualification repairs no additional text and leaves
unrelated fingerprints and evidence unchanged.

The migration is intentionally semantic-neutral: `observe(session, ticks)` delegates once
to the same `session.sustainTicks(ticks)` operation and adds telemetry only. It does not
shorten a stability window or alter the official server interaction. New asynchronous
conditions must use an `await*` contract instead of adding another temporal window.

The one-shot mechanical rewrite is complete and intentionally unavailable. Classify every
new or changed condition during review, then refresh the content-addressed attestations:

```text
java tools/harness/Gate.java --finalize-fixed-waits
```

The canonical gate verifies every migrated source hash, runner/descriptor binding, current
fingerprint, carried evidence hash, pin provenance, and the zero raw-debt ratchet.
