# M96 qualification cycle

`PageCapacityTwoThrashCycle` runs two fresh graphical-client/modded-server
replicas with pinned Aero, shared plan/nonce, strict camera, and an Aero-free
common/server closure.

Every aligned 56-byte page record is reparsed against the complete M74 census.
The gate requires cache2, four page calls, zero fallback, rebuilds in `{3,4}`
and, after the first retained record, `evicted[n]-evicted[n-1]` equal to the
current rebuild count. Per-mode counts remain dynamic evidence. Diagnostic mode
cannot qualify or write release evidence.

The canonical replicas selected stable modes 3 (4980 records) and 4 (4552
records), respectively. Mode assignment is not a release invariant. Exact EOF,
hashes, lifecycle, pinned provenance, clean server/client exit, and disposable
worktree cleanup remain mandatory.

The frozen semantic SHA-256 is
`96142417765b773152dc82aba8194765319c2c7bd987d513c5b8b8fd34b89acb`.
