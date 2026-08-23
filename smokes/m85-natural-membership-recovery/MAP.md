<!-- worldline-map-schema=1 -->
<!-- boundary=aero-cache-lifecycle -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=6afe38b10186f67d95eef5d1a1beca81bd168417d7d32d3579dfd654aae0445b -->

# M85 behavior map

M85 starts from the exact synchronized sixteen-identity Aero scene and fixed
camera qualified by M74/M78. After retained record 300, the client requests
that the server remove exact cell index zero. The server validates the block,
block entity, root nonce, and derived cell nonce before replacing the block
with air and acknowledging phase one.

The first retained fifteen-member record proves the natural topology change:

- renderer calls, queue calls, and queued membership change `16 -> 15`;
- four cached page calls become three cached page calls plus one direct fallback;
- the public M74 render/list counters change `0 -> 1` with that fallback;
- the cached-page count remains four and the removal record has no rebuild.

Thirty retained records after that transition, the client requests restoration
of the same coordinate. The server requires air and no block entity, restores
the registered block, assigns the exact original cell nonce, emits a dedicated
restore-state packet, and acknowledges phase two. The client buffers that state
until the flattened block update exists, then creates or validates the exact
block entity and applies the nonce.

The first restored sixteen-member record must show one rebuild, four cached
page calls, no direct fallback, and render/list counters back at zero. Every
other record has zero rebuilds. The M74 identity state remains `0x1010` with
mask `0xffff`. A fixed 52-byte post-seal artifact binds both request/event
pairs, plan, nonce, and transition rebuild counts.

Frozen trace:

```text
v1|design=2-fresh-same-plan-nonce-natural-membership-recovery-replicas|fixture=constant16-synced+client-marker+exact-camera|server-closure=aero-free|frame-limit=vanilla-max0+aero-pacer-off-runtime-gated|warm-path=enqueue16+flush2+cached4+pageCalls4+direct0+render0/list0+rebuild0|remove=request-after300+server-remove-index0+ack+client-air+membership16to15+pageCalls4to3+direct0to1+render/list0to1+rebuild0|restore=request-30-records-after-remove+server-restore-same-cell+buffered-state+ack+membership15to16+pageCalls3to4+direct1to0+render/list1to0+rebuild1|window=M74-min720intervals+12s|capture=M78-primitive-spans+page-counters+post-seal-52-byte-recovery-sidecar|per-record=identity16to15to16+state16/maskffff|stats=descriptive-transition-dynamic|arbitrary-addition-repeat-stale-cleanup-uninstrumented-cost-causality-regression-historical-lag=not-claimed|shutdown=clean
```

SHA-256: `6afe38b10186f67d95eef5d1a1beca81bd168417d7d32d3579dfd654aae0445b`.

Nonclaims: arbitrary additions, different cells, repeated cycles, stale client
block-entity cleanup, merge/repacking policy, uninstrumented or additive cost,
causal attribution, regression/improvement, inference, pixels, cross-machine
generality, combat relation, or historical lag reproduction.
