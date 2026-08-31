# M777-AERO-ADAPTIVE-PREWARM-REPEATABILITY Aero adaptive prewarm repeatability

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

Four fresh counterbalanced sessions compare cold compilation, blind prewarm, adaptive prewarm, and adaptive prewarm under pressure using identical fixed 600-frame journeys, aggregate safety bounds, three-of-four per-metric repeatability, four-of-four decoy reduction, and CPU/render-work attribution for host-pressure diagnosis.

## Qualification cycle

M777 runs sixteen fresh treatment clients after one template client. Each treatment performs first-sight turns, stable panel views, teleportation, and a spin; the pressured arm explicitly queues one speculative model before rendering so the governor must pause it and then preserve its urgent first use.

Expected signal: `scene=panels120x15+decoys,sessions=4,window=fixed600,arms=cold+blind+adaptive+pressured,journey=turn+hold+teleport+spin,adaptive=hidden4-MegaCrusher+hotness4,pressure=probe+urgent,miss=sync,hitch=safe,metrics=aggregate+3of4,decoys=4of4,attribution=cpu+render-work`.

Frozen semantic SHA-256: `27a0cd70f22302ef30827b597211def5f440ab8ffe985216da3b4f730539f888`.
