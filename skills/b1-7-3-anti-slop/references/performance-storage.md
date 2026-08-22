# Performance and storage review

## Prefer structural evidence

Measure nodes, inventories, slots, chunks, renderer calls, allocations, rebuilds, and queue depth before relying on milliseconds. Machine time is useful evidence but is noisy and does not prove complexity.

## Storage and item discovery

- Permit one explicit `O(n)` initial discovery or rebuild over the owned storage set.
- Maintain an incremental `ItemKey -> quantity and locations` index afterward.
- Make one-slot changes touch one snapshot entry and the affected index keys.
- Make queries proportional to returned types or locations, not total network capacity.
- Budget unavoidable rebuilds across ticks and expose remaining work.
- Define transaction, rollback, chunk unload, network split/merge, restart, and migration semantics.
- Test concurrent extraction and insertion for loss, duplication, stale counts, and partial commit.

## Tick and render hot paths

- Reject whole-world, whole-network, or whole-inventory scans reachable every tick unless a hard bound is proven.
- Reject per-frame file I/O, console logging, string formatting, reflection, and avoidable object creation.
- Preallocate bounded measurement buffers outside the sampled window.
- Keep instrumentation symmetric across experimental arms.
- Count complete intervals and disclose instrumentation overhead.

## Worldline scenarios

Use fresh isolated workspaces and actors. Vary scale, topology, chunk lifecycle, concurrency, restart, and failure timing. Gate correctness and structural work; report performance distributions without turning a small local sample into a causal or cross-machine claim.
