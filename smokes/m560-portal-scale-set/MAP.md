# M560 behavior map

One official session is written into far chunk `(20,20)` so the
Overworld portal is hundreds of blocks from spawn. Packet15 places
fourteen obsidian `49` cells as a `4x5` frame and flint-and-steel `259`
fills six interior cells with portal `90`. The actor stands inside the
interior for 120 ticks. The official server emits Packet9 dimension
`-1` and a corrected Packet13 pose.

The quantized destination `(floor(x), floor(z))` must lie within 128
blocks of `(floor(sourceX/8), floor(sourceZ/8))` and farther than 128
blocks from the Overworld source. Nether skylight is zero. Exact
generated-portal block coordinates are excluded because vanilla search
may pick a nearby column. This map does not re-qualify M132 activation,
M133 near-spawn traversal, M134 roundtrip, or M382 frame-ignite without
travel. Headless `B173WireClient` only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|profile=allow-nether-true|fixture=far-chunk20-obsidian49-frame4x5+flintsteel259|construction=packet15-fourteen-obsidian49+packet15-flint-259|entry=packet11-inside-portal90|residence=120ticks|transition=server-packet9-0-to-minus1|oracle=packet13-pose-quantized-8-to-1-not-nether-exists-not-m132-m133-m134-m382|dimension=0->-1,scale=8,source=325:66:331,expected=40:41,quantized=true,within=128,not1to1=true,sky=0,column=2,packet9=0->-1|disconnect=clean
```

Frozen semantic SHA-256:
`d7eb052e1bc5fe6a71f3850bd4fb75b9470be6a2767c6617fb41f7138c54c50b`.
