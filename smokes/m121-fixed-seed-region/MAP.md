<!-- worldline-map-schema=1 -->
<!-- boundary=m121-fixed-seed-region -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=c2a08f5e7e5ec2b6767afbc4b26409d586f2fd4ca296d199d823abe8b2b73d4f -->

# M121 behavior map

Two fresh official worlds use seed `17320110707` and load absolute chunks
`(-1,-1)` through `(1,1)`. Each world runs for 200 player heartbeats, saves,
restarts, and supplies all nine complete chunks to a fresh Packet51 reader.

The oracle traverses 294,912 block positions in a fixed chunk/x/z/y order.
It freezes the aggregate count of blocks other than air/water/lava, the exact
top Y/block ID/metadata of all 2,304 columns, and solid/empty occupancy on every
internal chunk seam. Interior position masks, IDs, metadata and fluid occupancy
are emitted as diagnostics and may differ after official scheduled and random
tick processing.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|region=3x3-absolute-chunks--1:1:-1:1|settle=200ticks+clean-restart+fresh-packet51|blocks=294912|columns=2304|solid-count=128529|solid-definition=not-air-water8+9-lava10+11|surface=efb4d6bcff8e9a7fa1911a3cbd8fae3303984e2cea888bfdd3c7290d9a6dd63e|internal-solid-seams=f3d4d525d4ca4b9e3a3f7e291f3a667c8e3c9d47eaaa10f9b10d63406d17792c|interior-position+ids+metadata+fluid-occupancy=diagnostic-not-frozen|decode=packet50+packet51-xzy|disconnect=clean
```

SHA-256: `c2a08f5e7e5ec2b6767afbc4b26409d586f2fd4ca296d199d823abe8b2b73d4f`.
