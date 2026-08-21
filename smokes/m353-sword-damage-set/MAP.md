# M353 behavior map

The fixture raises an isolated `7×7` grass platform, fences the perimeter
with item `85`, and places default mob spawner `52:0`. Packet7 while
holding wood sword `268`, iron sword `267`, then diamond sword `276`
strikes successive living type-`90` pigs. Two wood hits leave the pig
alive; later wood hits complete Packet38 status 3 plus Packet29. Iron and
diamond each leave the pig alive after one hit and then kill. Attacker
Packet8 stays `20`.

This map does not claim pork drops, knockback vectors, sheep, gold or
stone swords, or player-versus-player health.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-7x7-grass-platform+fence85-pen+default-spawner52|cause=packet7-sword268+packet7-sword267+packet7-sword276|wire=packet38-status2+packet38-status3+packet29+packet8-health20|oracle=wood-iron-diamond-hits-to-kill+attacker-packet8-20|column=17,platform=7x7-48grass,fence=85-perimeter,spawner=52:0,mob=type90,swords=268:2live+kill+267:1live+kill+276:1live+kill,health=20,hurt=packet38-status2,death=packet38-status3+packet29,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`cfaf1e0d3a43f1bb3a09cd6dadb2462a7d953de08671761856fc1080249424e4`.
