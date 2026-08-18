# M61 Furnace Output Retrieval

M61 closes the bounded furnace workflow by moving the exact M60 glass output
into an empty personal storage slot and proving both sides across a clean server
restart. The caller supplies only personal slot 9-44; the adapter owns the
window ID, action IDs, predictions, cursor, and legacy statistic correlation.

The completed M60 furnace remains open in the same window epoch, so its action
counter continues from four. Packet102 action 5 left-clicks owned output slot 2
with the exact glass `20x1:0` prediction. Vanilla removes the output, places it
on the cursor, and emits Packet200 statistic ID 16842772 with increment 1 before
Packet106 true. Worldline refuses the commit unless that exact side effect was
observed first.

Packet102 action 6 left-clicks the empty combined player destination and predicts
empty. For personal slot 36 the furnace mapping is combined slot 30. Packet106
true atomically publishes glass in the combined and canonical personal views
with an empty cursor. Every unrelated slot remains unchanged.

Qualification then closes through M58, saves and stops the official server, and
starts a new official process over the same workspace. A fresh client observes
glass in personal slot 36 and reopens the furnace with input, fuel, and output
all empty. This proves player persistence and persistent output removal.

## Boundaries

M61 claims only one glass item into an empty personal destination. It does not
claim output merging, stacks larger than one, arbitrary products, iron/fish
achievements, XP, shift/right clicks, rejected container recovery, or concurrent
mutation.
