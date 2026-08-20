# M353 sword damage set

M353 opens the official compound sword-damage boundary. Packet7 on a
living type-`90` pig while holding wood sword `268`, iron sword `267`,
and diamond sword `276` records material-specific health drops: two wood
hits leave the pig alive and later wood hits complete Packet38 status 3
plus Packet29; iron and diamond each kill after one living hurt.
Attacker Packet8 stays `20`.

This is distinct from M149 diamond-sword pig death only. It does not
claim pork drops, sheep, gold or stone swords, armor reduction, or
player-versus-player health. Headless `B173WireClient` only. No GUI. No
Aero.

Frozen semantic SHA-256:
`cfaf1e0d3a43f1bb3a09cd6dadb2462a7d953de08671761856fc1080249424e4`.
