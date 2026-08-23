<!-- worldline-map-schema=1 -->
<!-- boundary=aero-paired-experiment -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=41422dda87ca7a8ed192e8c23c9946c55518f87e123cf69d6b1662d689b3b500 -->

# M73 behavior map

M73 uses one exact activation message in both arms after a fixed real-client
warm-up. The server teleports to the fixed camera, sends the concrete plan, and
waits for a client acknowledgement that every target chunk is loaded and every
target cell is empty. The first arm establishes a fixed-seed world-spawn-anchored
compact sixteen-cell plan and the runner supplies that exact plan to the paired arm. `absent` records
the plan and mutates nothing; `present` places sixteen custom blocks, transfers
sixteen unique per-cell nonces through explicit server-authored content messages,
reconciles sixteen block entities, and returns
from the pinned Aero renderer for every identity. Both arms traverse four
server-tick treatment phases; present places four cells per phase.

The measurement bracket is identical by configuration: at least 720 renderer
TAIL frames, twelve seconds, and 45 threshold/GC/heartbeat-selected rows. Rows
come from stdout between trigger and completion and must survive in order in the
async file after normal shutdown. WorldFlush is excluded.

Frozen trace:

```text
v1|design=2-balanced-pairs-P/A+A/P|hosts=fresh-stationapi-server+real-aero-client|equivalence=same-mod-seed-name-heap-logger-pair-nonce|anchor=exact-activate+tracked-plan-ready|absent=planned16-placed0-rendered0|present=planned16-placed16-explicitly-synced16-rendered16|warmup=min300frames+5s|window=min720frames+12s|rows=min45-threshold-gc-heartbeat-selected|logger=threshold25-heartbeat200-sync-false|metrics=descriptive-paired-dynamic-only|causality-performance-historical-lag=not-claimed|shutdown=clean
```

SHA-256: `41422dda87ca7a8ed192e8c23c9946c55518f87e123cf69d6b1662d689b3b500`.

Nonclaims: causal or inferential effect, performance regression/improvement,
complete frame census, identical processes/world bytes, pixel visibility,
cell paging/batching, density response, combat relation, persistence, multiple
clients, cross-machine generality, or historical Aero lag reproduction.
