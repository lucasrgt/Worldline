# M56 Rejected Transaction Recovery

M56 makes ordinary personal clicks recoverable when the official server rejects
their prediction. Packet106 false is correlated by window and action. Worldline
immediately sends the vanilla Packet106 true re-enable acknowledgement, then
requires Packet104 window 0 followed by the Packet103 cursor sentinel.

The full view and cursor are staged rather than partially published. Only after
both validate does `B173InventoryTracker` atomically adopt the authoritative
state and `RemoteTransactionRejectedException` expose immutable
`RemoteRejectedTransaction` evidence.

The deterministic qualification probe is package-private and only substitutes
an empty prediction for an occupied-source/empty-cursor take. It cannot be used
by API callers to forge arbitrary window actions. The server rejects action 1,
resyncs slot empty/cursor stone, then accepts normal action 2; that acceptance
proves transaction processing was re-enabled.

## Boundaries

M56 does not expose caller-supplied predictions, rejected container clicks,
right/shift click, crafting, duplicate ACK control, or generic conflict merging.
