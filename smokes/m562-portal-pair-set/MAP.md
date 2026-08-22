# M562 behavior map

One official Overworld frame is built and traversed `0→-1`. After cooldown
return, a second east-facing frame is seated in the same 8:1 cell as the
landed portal. The second interior also emits Packet9 `0→-1` and exits
through the same generated Nether portal. Nearby portal `90` stays six
cells. Exact Overworld pair coordinates may reuse the source or a generated
return frame; they do not enter the frozen signal.

This map does not re-qualify M133 one-way travel, M134 single-portal
roundtrip, M560 scale arithmetic, or M561 distant search. Headless
`B173WireClient` only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|profile=allow-nether-true|fixture=two-east-obsidian49-frames-one-8:1-cell|construction=packet15-two-14x49-frames+flint259|cause=packet11-inside-portal90-twice|outbound=packet9-0-to-minus1-twice|nether=one-generated-portal-shared-exit|oracle=same-nether-cell+one-nether-portal-not-m134-roundtrip-not-m560-scale-not-m561-search|observation=nether-packet51+portal14x6-once|pair=shared-exit,scale=8,sameCell=1,column=10,netherPortals=1,dimensions=0->-1,0->-1,cooldown=220,travel=120|disconnect=clean
```

Frozen semantic SHA-256:
`d9c652c6452861ad1eda49be87a165111895c142551b462152ebb388ffb81b6c`.
