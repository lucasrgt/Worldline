<!-- worldline-map-schema=1 -->
<!-- boundary=m615-redstone-one-tick-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=21d4ac5bbdf54331350d6fd27043f140c2b7d3930a4466bb8c191d0c7fd93a8a -->

# M615 redstone one-tick set behavior map

A raised west-facing sticky piston `29:4` is powered from the cloned M142
east wall lever `69:1` on the support. Packet15 turns that lever on, and
Packet53 `69:9` is awaited at packet resolution. Packet15 then cuts the
lever immediately so the pulse is one `69:1 -> 69:9 -> 69:1` on-then-off,
not a held diode and not a delay-metadata cycle.

Sticky `29` starts its push from that short pulse and retracts without
pulling, leaving cobble `4` in the pushed cell (`0:0 -> 4:0`) with head
air (`4:0 -> 0:0`). Fresh login Packet51 keeps dropped cobble, retracted
`29:4`, and lever `69:1`.

This map is distinct from torch burnout `76:4 -> 75:4 -> 76:4` (M555),
repeater delay `93:3 -> 7 -> 11 -> 15` (M341), held piston extension
`33:4 -> 12` (M142), and retracted M557, which held a floor lever for four
ticks and never observed `69:9`.

Expected trace shape:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=west-sticky29+east-wall-lever69:1|cause=packet15-lever-cut-on-69:9|wire=packet53-lever69:1->9->1+sticky29-drop|oracle=tick-resolved-on-then-off+dropped-cobble+fresh-login|column=10,pulse=one-tick,drop=sticky-payload,piston=4:65:4:29:4,head=3:65:4:4:0->0:0,pushed=2:65:4:0:0->4:0,lever=5:64:4:69:1->9->1,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`21d4ac5bbdf54331350d6fd27043f140c2b7d3930a4466bb8c191d0c7fd93a8a`.
