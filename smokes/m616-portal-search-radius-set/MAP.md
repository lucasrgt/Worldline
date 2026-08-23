<!-- worldline-map-schema=1 -->
<!-- boundary=m616-portal-search-radius-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=b02e249055f3b7e33408a01b9ff5d87260c5eaf3e048ec11833a224d36a507f1 -->

# M616 behavior map

One official session builds the M382 `4x5` obsidian `49` frame, ignites
portal `90`, and traverses `0→-1` so the server creates a Nether exit.
A second Overworld frame is then built so its quantized 8:1 destination
lies 32 Nether blocks east of that existing interior: inside the
128-block search window and outside the 16-block create window.

Traveling the offset frame links to the existing Nether interior rather
than creating a new portal `90` column at the scaled destination.
Exact generated-portal coordinates stay out of the frozen signal because
vanilla search may pick a nearby column. Headless `B173WireClient` only.
No GUI. No Aero.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|profile=allow-nether-true|source=official-m382-obsidian49-frame+flint259|outbound=packet9-0-to-minus1|existing=generated-nether-portal90|offset-overworld=relog-shift-east-32+m382-frame|search-trip=packet9-0-to-minus1|effect=official-search-existing-nether-portal|oracle=not-m560-scale-only-not-m563-create-not-m562-pair|observation=live-packet51|dimensions=0->-1,shift=32,search=existing,radius=128,create-window=16,found=6x90,created=0,obsidian=49,portal=90,not-m560-scale-only,not-m563-create,not-m562-pair,persisted=true,clients=3,disconnect=clean,packet9=0->-1
```

Frozen semantic SHA-256:
`b02e249055f3b7e33408a01b9ff5d87260c5eaf3e048ec11833a224d36a507f1`.
