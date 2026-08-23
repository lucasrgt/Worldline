<!-- worldline-map-schema=1 -->
<!-- boundary=aero-paired-experiment -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=f3b298b76961b50be8e4695957f53c7ee1e735d394d0b26886e8c5164553adae -->

# M106 behavior map

M106 compares the exact pinned minimum page population at literals two and
five while retaining the same sixteen synchronized identities, four natural
page keys, paired plan/nonce, camera, and primitive recorder.

The automatic plan has two page populations at or above five and two smaller
populations totalling four instances. Therefore `minInstances=2` replays four
cached pages with no direct fallback, whereas `minInstances=5` replays two
cached pages and draws four instances directly. The M74 counters independently
move from render/list `0/0` to `4/4`.

Both arms keep pages enabled, unlimited cache, unlimited rebuild budget, and
TTL100000. Every aligned record must contain sixteen queue entries, two
flushes, no rebuild, no eviction, and no immediate call.

Frozen trace:

```text
v1|design=2-balanced-pairs-min2-min5+min5-min2-same-plan-nonce|fixture=constant16-synced+client-marker+exact-camera|server-closure=aero-free|frame-limit=vanilla-max0+aero-pacer-off-runtime-gated|config=pagesTrue+cacheNegative1+rebuildNegative1+ttl100000|min2=queueEntry16+flush2+pageCalls4+direct0+rebuild0+cached4+M74render0/list0|min5=queueEntry16+flush2+pageCalls2+direct4+rebuild0+cached2+M74render4/list4|both=immediate0+evicted0|window=M74-min720intervals+12s|capture=unified-60byte-sidecar+same-index-census|stats=paired-descriptive-dynamic|uninstrumented-cost-causality-regression-historical-lag=not-claimed|shutdown=clean
```

SHA-256: `f3b298b76961b50be8e4695957f53c7ee1e735d394d0b26886e8c5164553adae`.

Timing values and their directions remain descriptive and dynamic. M106 does
not claim generic threshold policy, cost attribution, causality, regression,
improvement, inference, pixels, or historical lag reproduction.
