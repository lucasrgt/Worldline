# M762-WALL-SIGN-SUPPORT-BREAK wall sign support break

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M762 opens the official wall-sign support break. A raised stone column top receives wall sign 68:5 on its east side face from item 323 through Packet15, and the supported attachment settles without any text editing. The supported sign stays block 68:5 across sustained observation while its support remains stone 1:0. Packet14 dig statuses then remove that supporting stone cell, so official BlockSign canBlockStay fails and the sign cell becomes air while Packet21 drops exactly one sign item 323 count 1 damage 0. Fresh login keeps the popped sign cell air. This freezes only the east-face attachment; standing signs 63, Packet130 text editing, and other facing metadata are deliberately outside the claim. This is distinct from M245 fresh-login Packet130 sign-text persistence, M350 sign text editing, M429 remaining attach faces, and M739 sugar-cane substrate invalidation.

## Qualification cycle

DataDrivenCycle rebuilds the raised stone column plus east wall sign fixture in two fresh official server JVMs. Each run places wall sign 68:5 against the stone top through item 323, observes the supported sign persisting beside intact support 1:0, digs the supporting stone with Packet14 begin and finish statuses, and requires air at both cells plus Packet21 item 323 count 1. Fresh login keeps the popped sign cell air. One official EOF is retried after a 5 second sleep. Headless B173WireClient protocol-14 only. No GUI. No Aero.

Expected signal: `column=17,support=4:71:4:1:0->0:0,sign=5:71:4:68:5->0:0,drops=packet21-323,persisted=true,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `8a45072ecf806340b62954df2f1636258a9a1be57128f8c1d78d760141d659d5`.
