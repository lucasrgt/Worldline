# FRONT-08 bed lifecycle coverage decision

## Decision

FRONT-08 is complete without a new milestone. The maintained M158, M330, and M567
official-server proofs already compose the requested Beta 1.7.3 bed, sleep, wake,
spawn-set, death, respawn, and persistence boundary. Creating M633 with the same
actions would duplicate official runtime rather than add coverage.

## Covered boundary

| Boundary | Canonical proof | Server-observable evidence |
| --- | --- | --- |
| Bed placement and daytime refusal | M158 `m158-bed` | Block `26` foot/head metadata `0/8`; daytime Packet3 `tile.bed.noSleep` |
| Night sleep entry | M158 and M330 `m330-bed-sleep-set` | Packet17 targets the bed head; head changes `26:8 -> 26:12`; Packet70 remains absent (`-1`) |
| Morning skip and wake | M330 | Head changes `26:12 -> 26:8`; actor returns standing; wake state survives a fresh login |
| Bed spawn assignment | M567 `m567-bed-spawn-set` | The fully-asleep wake path writes player bed `SpawnX/Y/Z` while leaving world spawn unchanged |
| Death and bed respawn | M567 | Cactus contact drives Packet8 health `20 -> 0`; Packet9 restores health `20` in dimension `0` beside the bed, not beside `level.dat` spawn |
| Persistence | M158, M330, and M567 | Bed halves remain valid after save; M567 reconnects and observes the player at the bed spawn |

The public behavior identities are `bed-sleep-skip` and `bed-spawn-respawn`.
Their reusable bindings are `worldline.b173server.B173BedAccess#await` and
`worldline.api.RespawnSession#respawn` respectively.

## Portable evidence

The versioned qualification lock binds the current inputs to these official observations:

- M158: fingerprint `f63e5f1b356d86fec167617c9a926bb5bddd49ac42eb713ae8a85dd8fe942a62`, observation `ab95c0893977d3774ddf9672b77063db206c52479e9645e917f6f0d42d49f2f0`.
- M330: fingerprint `e1425ad216b1162347f14384414e29133e4a4e5f4e5300860a9a9819357daac1`, observation `1415f89a64178b9c0135d108239ba04eb9fca293f9d8ee9005347624eb6842af`.
- M567: fingerprint `b07aa1c6a054043ccfd21fc5469634a58354332a19398a4f4bbbc85fe20c0968`, observation `aaad061b562df911b0b4c29784fe2beb4b0d5f1183dae8e29603cd3c2a838aed`.

The repository gate validates all three tracked proof envelopes and their current
fingerprints. FRONT-08 therefore reuses portable evidence and does not launch an
additional official-server cycle.

## Nonclaims

This decision does not extend the three proofs to invalid or obstructed bed fallback,
multi-player partial-sleep voting, Nether bed explosions, rain Packet70 reasons, or
client rendering. Nether bed explosion already has the separate M359 boundary; the
remaining items stay explicit future surfaces rather than silent coverage claims.
