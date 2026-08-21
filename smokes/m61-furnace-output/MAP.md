# M61 Furnace Output Map

| Boundary | Exact evidence |
| --- | --- |
| Epoch | The open M60 furnace continues with container actions 5 and 6 |
| Take | Packet102 action5 predicts glass20x1 from owned output slot2 |
| Side effect | Packet200 stat16842772 increment1 precedes Packet106 true |
| Store | Packet102 action6 predicts empty and stores glass at combined30/personal36 |
| Atomic state | Active view, canonical window0, and cursor commit together after each ACK |
| Peer | Selected hotbar state changes from empty to glass20 |
| Close | M58 accepted window0 proof closes the exact extracted furnace view |
| Restart | Fresh player window has glass36 and fresh furnace view has owned0/1/2 empty |

M61 does not claim merges, larger outputs, arbitrary products, XP, achievements,
shift/right clicks, rejection recovery, or concurrent furnace mutation.
