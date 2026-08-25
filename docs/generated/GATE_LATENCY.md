# Gate Latency Trend

This generated view mirrors `quality/gate-latency.properties`. The canonical gate rejects a hot
run at or above 4 seconds and a cold run at or above 240 seconds. Selected hot stages also fail
when they exceed twice their measured baseline. A run is hot only when every verification stage
is restored and no module, test module, test suite, smoke runner, or aggregate qualification is
compiled, executed, or validated.

| Date | Cache state | Wall time (ms) |
| --- | --- | ---: |
| 2026-08-24 | cold | 132624 |
| 2026-08-24 | hot | 2176 |

| Hot stage | Measured baseline (ms) |
| --- | ---: |
| smoke-discovery | 807 |
| release | 10 |
| release-artifacts | 75 |
| modules | 900 |
| tests | 400 |

The hot value is re-pinned from a clean committed tree after structural cache changes.
