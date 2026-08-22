# M464 behavior map

A raised-stone pad seeds TNT `46`, flint-and-steel `259`, golden apple
`322`, and a mob spawner `52`. After a clean save, the spawner is
retargeted to `Creeper`. Night `14000` plus proximity fuse emit Packet24
type `50` and Packet60 at strength `3`, which drops Packet8 health. A
golden-apple restore plus daylight then Packet15-ignites TNT on a
surviving obsidian anchor. The actor stands 3 blocks away (movement cap
9) and records Packet60 at strength `4` plus a second Packet8 drop.

This family is player-hurt-from-explosions, not a block crater. It does
not re-qualify M137 TNT destroyed cells, M391 wool/dirt crater, or M359
Nether-bed Packet60 strength `5`.

Headless `B173WireClient` protocol-14 only. No GUI. No Aero.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone-pad+obsidian-anchor+tnt46+flint259+gapple322+creeper-spawner52|cause=nbt-entityid-creeper+time-14000+proximity-fuse+packet15-ignite+stand-3|wire=packet24-type50+packet60-strength3+packet8+packet60-strength4+packet8|oracle=player-hurt-from-explosions-tnt4+creeper3-not-crater-not-bed5|column=17,support=4:71:4:1:0,tnt=46+flint259+packet60-strength4+packet8,creeper=type50+packet60-strength3+packet8,gapple=322,survived=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`57b40f0692f328b32fc02a568a0f47854d1d21fda0e2ed383262bf03c2b8c078`.
