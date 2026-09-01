# M743-REDSTONE-ORE-DUST-DROP redstone ore dust drop

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M743 opens the official redstone-ore harvest boundary without asserting one random draw, a total quantity, or a probability. A placed unlit ore 73:0 on the raised stone column is fully broken with iron pickaxe 257; the cell must become air 0:0 and at least one Packet21 dropped-item entity of exactly redstone dust 331 with count one and damage zero must spawn within forty bounded observations. Packet21 describes an individual dropped-item entity stack, so the frozen evidence records that protocol-observable membership and the observation bound only; the number of dust entities and their total quantity are explicit nonclaims. This is distinct from M229 unlit placement, M300 cobble plus coal plus diamond pick breaks, M511-SW controlled trigger, and M571 glow fade.

## Qualification cycle

DataDrivenCycle executes RedstoneOreDustDropSmoke twice on the official b1.7.3 server JAR. Each run rebuilds the raised stone column from the deterministic dirt-under-water foundation, places redstone ore item 73 on top, observes live 73:0, selects iron pickaxe 257, sends the full Packet14 break, requires the air cell, then polls the exact count-one dust stack 331x1 through BoundedAttempts until an equal Packet21 entity spawns within forty bounded observations. Headless protocol-14 B173WireClient only. No GUI. No Aero.

Expected signal: `column=17,support=4:71:4:1:0,ore=4:72:4:73:0->0:0,pick=iron257,dust=packet21-331x1,wait=bounded<=40,clients=1,disconnect=clean`.

Frozen semantic SHA-256: `473194f1f2bf89eb01ae9058b28e52d6844576ce5122675f531d9f8096bfcf0f`.
