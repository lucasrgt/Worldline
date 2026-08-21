# M14 Chunk Backlog and Caller Semantics

M14 instruments the exact chunk compiler boundary left open by M13. It explains
why the fixed-camera empty control continues compiling, identifies the caller's
retry contract, and evaluates one bounded policy without changing the pinned
Aero checkout.

## Caller contract

`GameRenderer.renderFrame(float, long)` calls
`WorldRenderer.compileChunks(camera, false)`. While the method returns `false`,
the caller invokes it again until the frame deadline expires. The principal
capture path is therefore non-forced; `false` means retry immediately, not defer
until a later frame. This corrects M13's earlier description of those calls as
forced and explains why its always-active governor created a hot loop while
skipping work.

## Initial dirty backlog

Both M14 modes disable the Aero test fixture and start a fresh world with seed
`17320110707`. After the same 60-tick warmup, the vanilla measured window still
begins with thousands of dirty `ChunkBuilder` instances. In the qualifying
windows, every vanilla call was non-forced and returned `false`, rebuild counts
closely tracked calls, and the queue remained in the thousands while declining.
Most measured frames had neither builder invalidation nor `markDirty` activity.

The stable-camera compilation pressure is therefore primarily the initial
dirty-builder queue draining over many frames, with occasional new dirtiness
superimposed. M14 does not claim that this backlog explains every historical
random lag spike.

## Bounded non-retry prototype

The smoke-only prototype uses vanilla's `DirtyChunkSorter` and consumes its
highest-priority end. Once per rendered frame it rebuilds at most two real
candidates, marks them clean, removes them from `dirtyChunks`, and returns
`true`. Each qualifying 200-frame window made exactly 200 calls, returned
`true` 200 times, and performed exactly 400 rebuilds. No retryable `false` was
emitted.

This validates the caller-level shape of a non-retry budget, not a production
patch. It drains the backlog more slowly and can increase terrain update
latency. The prototype also lacks a visual-equivalence oracle and remains
smoke-only. Frame and compile timing distributions are reported as exploratory
measurements, not frozen release claims. Repetitions changed 25 ms threshold
counts in both directions, so M14 makes no performance-win claim.

## Next boundary

M15 should turn the caller result from a Boolean ambiguity into an explicit
accepted-work/deferred-work contract at an Aero-owned integration point, then
measure queue age and visible-chunk readiness. Promotion requires visual
correctness and latency evidence in addition to eliminating immediate retries.
