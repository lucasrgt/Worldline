<!-- worldline-map-schema=1 -->
<!-- boundary=m308-fragile-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=016e31ada167a1772c3c0ec4d610d946ddf26bc0a93c97ad494019ab72c97ce5 -->

# M308 behavior map

The M193 raised stone column hosts three official fragile cells in one
session. Packet15 places ice item `79` on a west pad; Packet14 breaks
that ice. Packet15 places glass item `20` on a north riser; Packet14
breaks that glass to air `0:0` with no Packet21 glass drop. Packet15
then places a second ice on the support top beside floor torch `50:5`.
Official random ticks convert that ice to still water `9:0`. The
Packet14 ice leftover also persists as still water `9:0` beside that
source. Exact melt delay is not hashed.

This map does not re-qualify ice placement without break (M193) or glass
placement without break (M196). Headless `B173WireClient` only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+ice79-break+glass20-break+ice79-torch-melt|cause=packet14-break-ice79+packet14-break-glass20+packet15-item50|wire=packet53-ice79:0->9:0+packet53-glass20:0->0:0+packet53-melt79:0->water9:0|oracle=live-leftover+no-packet21-glass20+official-random-tick-melt+fresh-login|column=17,support=4:71:4:1:0,west=3:71:4:1:0,ice=3:72:4:79:0->9:0,pad=5:71:4:1:0,torch=5:72:4:50:5,melt=4:72:4:79:0->9:0,north=4:71:3:1:0,riser=4:72:3:1:0,glass=4:73:3:20:0->0:0,drop=no-packet21-20,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`016e31ada167a1772c3c0ec4d610d946ddf26bc0a93c97ad494019ab72c97ce5`.
