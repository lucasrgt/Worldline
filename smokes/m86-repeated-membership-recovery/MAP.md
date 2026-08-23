<!-- worldline-map-schema=1 -->
<!-- boundary=aero-cache-lifecycle -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=841b311c16d11cbbe669756fd0fc020c4371b650ad9c185d8ab717c7217abc44 -->

# M86 behavior map

M86 repeats the exact M85 natural membership recovery on cell index zero. Two
fresh replicas share seed `85085`, plan, root nonce, fixed camera, page
configuration, warmup, and complete M74/M78 recording window.

The client sends four generation-bound requests. Generation one removes and
restores the cell; generation two then removes and restores that same cell.
Every request and ACK carries `{x,y,z,root,generation,operation}`. Restore-state
messages additionally carry the derived cell nonce and generation. Duplicate,
skipped, reordered, cross-generation, wrong-coordinate, or wrong-nonce state
fails closed.

Request one is sent after retained record 300. Every later request waits at
least thirty records after the preceding observed transition. The complete
records prove membership `16 -> 15 -> 16 -> 15 -> 16`. Each removed interval
has three page calls, one direct fallback, one public render/list call, four
cached pages, and zero rebuilds. Each restore transition has four page calls,
no direct fallback or public render/list call, four cached pages, and exactly
one rebuild. All other records have zero rebuilds. M74 state stays `0x1010`
with mask `0xffff` throughout.

A fixed 60-byte post-seal sidecar stores magic, schema, length, root nonce,
plan, and four request/event pairs. Topology and rebuild evidence deliberately
come from the corresponding complete M78 records, not phase-sensitive live
counters. The runner reparses all three artifacts, exact EOF, hashes, marker
order, server closure, clean disconnect, and clean worktrees.

Frozen trace:

```text
v1|design=2-fresh-same-plan-nonce-two-generation-membership-recovery-replicas|fixture=constant16-synced+client-marker+exact-camera|server-closure=aero-free|frame-limit=vanilla-max0+aero-pacer-off-runtime-gated|warm-path=enqueue16+flush2+cached4+pageCalls4+direct0+render0/list0+rebuild0|sequence=remove1+restore1+remove2+restore2-on-same-index0-with-generation-bound-ack/state|spacing=request1-after300+each-next-request-30-records-after-prior-event|membership=16to15to16to15to16|removed=pageCalls3+direct1+render/list1+rebuild0|restored=pageCalls4+direct0+render/list0+rebuild1|window=M74-min720intervals+12s|capture=M78-primitive-spans+page-counters+post-seal-60-byte-recovery-sidecar|per-record=state16/maskffff|stats=descriptive-transition-dynamic|arbitrary-addition-more-than2-generations-multicell-concurrency-stale-cleanup-uninstrumented-cost-causality-regression-historical-lag=not-claimed|shutdown=clean
```

SHA-256: `841b311c16d11cbbe669756fd0fc020c4371b650ad9c185d8ab717c7217abc44`.

Nonclaims: arbitrary additions, more than two generations, different or
multiple cells, concurrency, stale client block-entity cleanup, merge or
repacking policy, persistence, uninstrumented/additive cost, causality,
inference, regression/improvement, pixels, cross-machine generality, combat,
or historical lag reproduction.
