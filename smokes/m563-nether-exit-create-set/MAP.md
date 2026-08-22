# M563 behavior map

One official session builds the M382 `4x5` obsidian `49` frame, ignites
portal `90`, and traverses `0→-1`. After leaving the generated Nether
portal for 220 ticks, the actor relogs 32 blocks east in dimension `-1`
so the scaled Overworld destination is outside the 128-block search
window of the source frame.

A second M382 frame is lit in that far Nether column. Returning through
it makes the official server create a new Overworld portal: six portal
`90` cells plus fourteen obsidian `49` frame cells. That created frame
is not the M134-reused source interior. Headless `B173WireClient` only.
No GUI. No Aero.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|profile=allow-nether-true|source=official-m382-obsidian49-frame+flint259|outbound=packet9-0-to-minus1|nether=leave+cooldown+relog-shift-east-32+m382-frame|return=packet9-minus1-to-0|effect=official-create-overworld-portal-obsidian49-plus-portal90|oracle=not-m134-reuse-of-source-frame-not-m561-search-not-m562-pair|observation=live-packet51|dimensions=0->-1->0,column=10,source=4:65:4,shift=32,created=6x90+14x49,obsidian=49,portal=90,not-source,not-m134-reuse,not-m382-activation-only,not-m561-search,not-m562-pair,cooldown=220,persisted=true,clients=3,disconnect=clean
```

Frozen semantic SHA-256:
`6a31a5c30bf7a861c626da550e1989e4d2c38f0a32cd4607e27a9093fa6a268d`.
