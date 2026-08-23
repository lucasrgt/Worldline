# M58 Remote Window Lifecycle Map

| Boundary | Exact evidence |
| --- | --- |
| Fixture | Official player NBT seeds chest `54` in slot 36; a bounded nearby solid/replaceable pair receives it |
| Open | Existing Packet15 activation is paired with Packet100/104 single-chest state |
| Close | Client sends Packet101 with the tracked active window ID, then proves window 0 by accepted no-op |
| Fail closed | A second close is rejected locally because no remote window remains active |
| Server acceptance | A later `/give` arrives as player-window Packet103 and actions 1/2 are accepted |
| Persistence | The restored personal slot contains one stone entry after save |

M58 does not claim a Packet101 server acknowledgement, container writes, forced-close
distance behavior, double chests, workbenches, furnaces, or generic window types.
