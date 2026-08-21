# M67 Chest Retrieval Evidence Map

| Boundary | Exact evidence |
| --- | --- |
| Fixture | M59's accepted actions put exact stone `1x1:0` into single-chest slot 0 |
| Reopen | A fresh Packet100/104 pair on the same server exposes chest0 stone and empty personal36 |
| Retrieval | Packet102/106 action 1 takes chest0; action 2 stores into combined54/personal36 |
| Atomic state | The active 63-slot view, canonical window0 view, and cursor commit together after each ACK |
| Close | M58 window0 proofs close the stored and retrieved snapshots with personal actions 1 and 2 |
| Restart | A clean new official-server process exposes empty chest0 and exact stone in personal36/combined54 |
| Persistence | The final player NBT contains one inventory entry |

M67 is restricted to exact stone, an empty destination, left clicks, a single
chest, and an empty cursor. It does not claim merging, splitting, shift/right
clicks, arbitrary items, rejected container recovery, concurrent mutation, or
generic containers.

Frozen expected signature SHA-256: `cbeb29b97d06faa167bb524366feb7b9d1a92fa03edeb432470d7f1ff0a7b469`
