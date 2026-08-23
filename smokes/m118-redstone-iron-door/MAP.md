<!-- worldline-map-schema=1 -->
<!-- boundary=m118-redstone-iron-door -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=e2000f240f0dce5e5fe233611cca6053e50b31c57113fd564387a00f527d7573 -->

# M118 behavior map

The official player inventory contains stone 1, lever 69 and iron door item
330. The selected-item Packet15 creates two block-71 states above the column:
bottom metadata 0 and top metadata 8. The side lever remains metadata 1 after
the stabilization boundary.

Empty-hand activation toggles the lever and the official server publishes
three Packet53 changes: lever `1 -> 9`, door bottom `0 -> 4`, and door top
`8 -> 12`. A fresh Packet51 must retain all three values, and the ordered
full-chunk delta admits no other changed state.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=stone-column+lever69+iron-door-item330-block71|settle=200+10ticks|cause=packet15-lever-activate|effect=packet53-iron-door-open|observation=fresh-login-packet51|column=10,lever=5:64:4:1->9,door=4:65:4:0->4,top=8->12,states=3:25ac35cc392872a6b74071fed09a2a9e647b4c9a7b2896477af9c94719441191|disconnect=clean
```

SHA-256: `e2000f240f0dce5e5fe233611cca6053e50b31c57113fd564387a00f527d7573`.

Packet15 is request evidence only. Packet53 and the reload Packet51 are the
server-authoritative consumer-state oracles.
