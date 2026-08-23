<!-- worldline-map-schema=1 -->
<!-- boundary=state-world-differential -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=e968302dd1f7e0be9f133f8956a2512b5203382b0ba53d07fbb0b1028b5586fe -->

# M522-SW behavior map

- Player NBT main slots `0` and `1` map to personal-window slots `36` and `37`.
- Protocol packet 102 carries window `0`, slot, left button `0`, an action
  short, and the client's predicted clicked stack.
- An occupied slot clicked with an occupied cursor exchanges the two stacks;
  the clicked slot receives the cursor stack and the old slot stack becomes the
  cursor. Packet 106 must accept each correctly predicted action.
- A mismatched prediction does not undo the real server click. Packet 106
  rejects it and the server sends authoritative Packet 104 window state plus a
  cursor update. M522 restores that applied take before persistence checks.

The smoke covers distinct occupied-stack exchange, same-slot take/place,
rejection recovery, clean save, and same-player relogin. It does not claim
merge, split, shift/right click, armor, other windows, drops, or respawn.

Frozen expected signature SHA-256: e968302dd1f7e0be9f133f8956a2512b5203382b0ba53d07fbb0b1028b5586fe

## Frozen semantic signal

`slots=36<->37,items=stone<->dirt,actions=1+2+3,reject=4-false,resync=applied-take,restore=5-accepted,relogin=swapped,persisted=2,cursor=empty`
