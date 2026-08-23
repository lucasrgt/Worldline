<!-- worldline-map-schema=1 -->
<!-- boundary=m367-piston-motion-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=eeb597ce51f18b3841a00e606375efae5dfb531672564e34670469f420f304a8 -->

# M367 behavior map

The cloned M142 west-facing piston family occupies one raised stone column.
Normal piston `33:4` sits on the support at `(4,65,4)` with stone `1:0` in
front and a side lever `69:1`. Two south pads host the cloned M144 sticky
arm at `(4,65,6)`: sticky piston `29:4`, stone payload, and a second lever.

Empty-hand Packet15 extends piston `33` (`33:4 -> 33:12`, head `34:4`,
displaced stone). A second Packet15 retracts it (`33:12 -> 33:4`, head
air, stone retained). The sticky lever then extends `29:12` / head
`34:12` and retracts with an official sticky pull (`34:12 -> 1:0`,
destination air). Fresh login Packet51 keeps both final arms.

This map is distinct from M293/M294 place-only (`29:1` / `33:1` with no
lever motion) and from shipping M142-M144 1:1 single-arm cycles.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=piston33-west+sticky29-west|settle=200+10ticks|cause=packet15-lever-activate+deactivate|effect=official-piston33-extend+retract+sticky29-pull|observation=fresh-login-packet51|column=10,extend=33:4->12,retract=33:12->4,sticky-pull=29:12->4,piston=4:65:4:33:4->12->4,head=3:65:4:1:0->34:4->0:0,pushed=2:65:4:0:0->1:0->1:0,sticky=4:65:6:29:4->12->4,sticky-head=3:65:6:1:0->34:12->1:0,sticky-pushed=2:65:6:0:0->1:0->0:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`eeb597ce51f18b3841a00e606375efae5dfb531672564e34670469f420f304a8`.
