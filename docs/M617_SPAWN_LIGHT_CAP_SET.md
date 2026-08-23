# M617-SPAWN-LIGHT-CAP-SET spawn light cap set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M617 opens the official hostile spawn-light cap. An unlit 7x7 grass pad at night permits nearby Packet24 type 50 or 54. Four floor torches 50:5 on the same copied pad raise block light to >= 8 and keep those types absent. Cover and substrate stay identical; only torch light changes. Distinct from M435 identity-only natural hostiles, M569 spawner delay/range, and M564's 49-torch carpet.

## Qualification cycle

DataDrivenCycle rebuilds the raised grass pad in two fresh official server JVMs. Each run copies the unlit world, requires a nearby type 50 or 54 at block-light 0, then places four sparse floor torches on the copy, reloads Packet51 light, and requires block-light >= 8 plus Packet24 50/54 absence near the pad. Headless protocol-14 only. No GUI. No Aero.

Expected signal: `column=17,platform=7x7-48grass,spawners=4:72:4:52:0+5:72:4:52:0,entityid=Creeper+Zombie,dark=type50-or-54,torch=50:5x4,dark-light=0,lit-light>=8,torch-arm=absent,night=14000,clients=4,disconnect=clean`.

Frozen semantic SHA-256: `7b7f1afdfd24186f8c299874e34b09f3ab9ff8edd782a7b955a91ebd8d042d20`.
