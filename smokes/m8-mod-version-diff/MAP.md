<!-- worldline-map-schema=1 -->
<!-- boundary=mod-version-difference -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=1e04f386003b84932be53ef8c7525b2ecd0205502457265355a278c9c0d1eca1 -->

# M8 Mod Version Differential Evidence Map

## Claim

Worldline can execute the same controlled scenario against two descriptor-
compatible versions of one mod, persist exact artifact/trace provenance, and
report the first behavioral divergence from durable results.

## Scenario matrix

The M8 runner starts only after the mapped controlled client has matched two
fresh direct official-JAR oracle processes. It writes each mod JAR twice using
fixed ordering, timestamps, STORED entries, and CRCs, then requires identical
bytes per version.

| Run | Expected block 65 sequence after ticks 1-3 |
| --- | --- |
| No-mod baseline, twice | `0, 0, 0` |
| `worldline.version-probe@1.0.0`, twice | `20, 20, 20` |
| `worldline.version-probe@1.1.0`, twice | `0, 41, 41` |

Six game JVMs and eight neutral CLI JVMs prove paired run determinism,
byte-identical repeated v1 results, exact baseline/version divergences, exact
v1/v2 divergence, equal-result exit 0, and corrupt-result exit 1.

The first v1/v2 divergence is exactly:

```text
kind=VALUE
record.index=1
record.label=tick1
field.index=1
field=block65
left=20
right=0
role=BLOCK_ID_READ
invariant=block-conservation
```

The frozen evidence SHA-256 is
`aa0b9a653814de9b94b66175be673f85aaa218120728bb57c6087f921b4d28dd`.
Generated JARs, traces, results, classes, and evidence remain under ignored
`.worldline/`.

## Boundary

The mod deltas are explicitly enabled controlled modifications; vanilla
equivalence is owned by the immediately preceding client oracle cycle. M8
qualifies two versions on b1.7.3 / API 1 only. See `docs/M8_RESULTS.md` for the
portable result semantics and non-claims.
