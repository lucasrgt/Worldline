<!-- worldline-map-schema=1 -->
<!-- boundary=m401-remaining-redstone-wire -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=b37e39c18b5b7ba396453c42ce9a726e1b0b51ab26949df34031ab9c9ddcd82e -->

# M401 behavior map

One official session places remaining redstone-wire `55` as three connection
shapes on a raised stone fixture. Packet15 of dust item `331` writes a
four-arm cross of unpowered `55:0` on the center support and its north,
south, east, and west pads. A gapped east-west line of three `55:0` cells
sits two blocks south. A south-east elbow of three `55:0` cells sits two
blocks east. Neighbor masks resolve to `nsew`, `ew`, and `se`. All three
centers survive a clean save plus fresh login.

This map does not re-qualify single-cell `55:0` (M243), lever-to-wire power
(M116/M126), or rail-power bits (M309). Headless `B173WireClient` only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+wire55-cross+wire55-line+wire55-elbow|cause=packet15-item331-cross+line+elbow|wire=packet53-wire55:0-nsew+55:0-ew+55:0-se|oracle=connection-shape-set+fresh-login|column=17,support=4:71:4:1:0,cross=4:72:4:55:0:nsew,line=4:72:7:55:0:ew,elbow=7:72:4:55:0:se,persisted=55:0+55:0+55:0,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`b37e39c18b5b7ba396453c42ce9a726e1b0b51ab26949df34031ab9c9ddcd82e`.
