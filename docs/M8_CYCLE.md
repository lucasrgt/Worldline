# M8 Differential Mod Testing GO Audit

Status: GO.

## Promotion evidence

| Requirement | Evidence | State |
| --- | --- | --- |
| Durable result | Canonical `.wlmtest` binds mod descriptor, artifact hash, and trace with two checksums | PASS |
| Runtime-independent diff | Product unit and CLI tests compare parsed results without adapter/game classes | PASS |
| Version provenance | Same mod ID with versions `1.0.0` and `1.1.0` remains explicit in every diff | PASS |
| Reproducible packages | Two independently written STORED JARs per version are byte-identical | PASS |
| Reproducible execution | Two baseline, two v1, and two v2 clients produce byte-identical paired traces | PASS |
| Exact behavioral delta | v1/v2 first diverge at `tick1.block65`, ordered `20 -> 0` | PASS |
| Baseline deltas | Baseline/v1 first diverges at tick 1; baseline/v2 first diverges at tick 2 | PASS |
| Result determinism | Repeated v1 records are byte-identical; equal comparison exits 0 | PASS |
| Corruption rejection | A modified result fails parsing before comparison | PASS |
| Vanilla baseline | Required controlled-client evidence matches the direct official-JAR oracle before M8 | PASS |

The frozen full evidence-report SHA-256 is:

```text
b08aa9f46b2d8522e6b8ac991553b2b6f946a63190d5956e59cbf6d544eb8938
```

The report freezes all three trace signatures, both JAR hashes, both result
hashes, the baseline/version diff hashes, the version/version diff hash, and
corruption rejection.

## Qualification

The canonical `java tools/harness/Verify.java --smoke` gate passed from both
the prepared workspace and a cold client reconstruction. The cold run invoked
RetroMCP decompile/recompile, rebuilt the adapter and every M8 artifact, and
preserved the frozen evidence report. Rebuilt mapped `Minecraft.class` retained
SHA-256 `f3aba176750d89e28559b9c85b070d1819ed310b83a6703df002e768ad8ee14a`.
The redundant pre-reconstruction directory was sent to the Windows Recycle Bin.
