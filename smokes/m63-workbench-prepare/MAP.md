# M63 Workbench Preparation Map

| Boundary | Exact evidence |
| --- | --- |
| Source | Exact planks5x3 in personal36 / combined37 |
| Take | Packet102 action1 left-takes all three planks |
| Wire | Shared encoder byte fixture freezes slots1/2/3, button1, actions2/3/4, shift false, null |
| Width | Packet102 actions2-4 are ACK-correlated to the local one/two/three-wide model |
| ACKs | Every mutation commits only on matching Packet106 true |
| Cursor | Empty -> 3 -> 2 -> 1 -> empty |
| Model | Two-wide pressure plate72 then three-wide wooden slabs44:2 |
| Safety | Public Packet101 close fails locally while result/matrix are occupied |

The byte fixture proves right-click encoding; Packet106 accepts each null return
but does not independently reveal cursor, matrix, or SlotCrafting state. M63
therefore treats those as ACK-correlated adapter models. M64 must confirm the
final slabs prediction by taking output. Generic recipes/clicks remain absent.

Frozen expected signature SHA-256: `9fd2fb1869b8221cc5e2c9173a548224fb65ca6c6dc9c37858eeb88cd24bf289`
