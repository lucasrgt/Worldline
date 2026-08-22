# M57 Personal Crafting Map

| Boundary | Exact evidence |
| --- | --- |
| Fixture | Official-format player NBT places both peers near the exact level.dat spawn and one log in actor slot 36 before first login |
| Personal layout | Window 0 has result slot 0, 2x2 matrix slots 1-4, and hotbar slot 36 |
| Recipe | One log `17x1:0` in matrix slot 1 derives planks `5x4:0` in result slot 0 |
| Wire | Four left/non-shift Packet102 requests are correlated with Packet106 accepted actions 1-4 |
| Prediction | Ingredient placement and result consumption update the multi-slot model only after acceptance |
| Peer | Independent client observes the initial log and terminal planks in the actor's hand |
| Audit | A stale action 5 forces authoritative Packet104/cursor Packet103 recovery with an empty grid and planks cursor |
| Persistence | Clean save retains one player-inventory entry after the cursor and matrix are emptied |

M57 does not claim general recipes, metadata variants, right/shift clicks, container-item remainders,
workbench crafting, or remote container transactions.

Frozen expected signature SHA-256: `a7ca218db3ec5f4fe14ee8f7ec54955d49eb343c9185c62ab6982add0a2e8c7d`
