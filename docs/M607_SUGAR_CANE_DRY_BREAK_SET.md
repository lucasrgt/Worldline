# M607-SUGAR-CANE-DRY-BREAK-SET sugar cane dry break set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M607 opens the official sugar-cane dry-break pop. A raised dirt cell beside still water 9:0 receives reed 83:0 from item 338. Packet15 of stone replaces that hydrating water, then Packet15 of stone against the cane east face neighbor-updates BlockReed. Official canBlockStay fails, the cane cell becomes air, and Packet21 drops reed item 338. Those air, dirt, and neighbor cells remain after a clean save plus fresh login. This is distinct from M159 water-adjacent growth and M384 cactus plus sugar-cane height growth.

## Qualification cycle

DataDrivenCycle rebuilds the raised dirt-and-still-water cane fixture in two fresh official server JVMs. Each run plants cane 83 beside still water 9, replaces the water with stone 1, places stone 1 on the cane east face, and requires Packet53 air plus Packet21 item 338. Fresh login keeps the cane cell air beside dirt 3 and neighbor stone 1. One official EOF is retried after a 5 second sleep. Headless B173WireClient protocol-14 only. No GUI. No Aero.

Expected signal: `column=17,dirt=4:72:4:3:0,water=5:72:4:9:0->1:0,cane=4:73:4:83:0->0:0,neighbor=5:73:4:1:0,drops=packet21-338,persisted=true,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `fc510e49797e7209a3a92a1d34c5ddd918cbfc34b4942d10af06278b6eaf57f4`.
