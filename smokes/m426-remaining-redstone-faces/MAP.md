<!-- worldline-map-schema=1 -->
<!-- boundary=m426-remaining-redstone-faces -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=1bb55855bc7d7a3c3f9eef22fd7e235e02c3e5220a782fb29ed29a27bb69b44e -->

# M426 behavior map

Official server symbols:

- `net.minecraft.src.BlockLever` Packet15 on west/south/north writes
  unpowered `69:2`, `69:3`, and `69:4`. UP-face placement writes ground
  metadata `5` or `6` from `World.rand`; this map hashes ground
  attachment, not that orientation bit. East `69:1` is M242/M340.
- `net.minecraft.src.ItemRedstoneRepeater` Packet15 of item `356` uses
  look yaw to write remaining unpowered facings `93:0` (north), `93:1`
  (east), and `93:2` (south). Empty-hand Packet15 adds delay bits on the
  east cell (`93:1->5`). Torch `76:5` on the west input becomes powered
  `94:5` after scheduled diode ticks. West delay `3->7->11->15` is M341.

This map does not re-run M340 lever latch `69:1->9->1`, M341 west delay
tunes, M399 stone-button `77` faces, or M170's west 1-tick pulse.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+lever69-west-south-north-ground+repeater93-north-east-south+torch76|cause=packet15-item69-west+south+north+up+packet15-item356-look180+-90+0+empty-hand-packet15-tune+packet15-item76|wire=packet53-lever69:2+69:3+69:4+69:floor+repeater93:0+93:1->5+93:2+94:5|oracle=remaining-wall-ground-lever-faces+remaining-delay-facing-93-94+fresh-login|column=17,support=4:71:4:1:0,west=3:71:4:69:2,south=4:71:5:69:3,north=4:71:3:69:4,ground=4:72:4:69:floor+5:72:4:69:floor,north93=6:72:4:93:0,east93=6:72:5:93:1->5,south93=6:72:6:93:2,powered=6:72:5:94:5,torch=5:72:5:76:5,persisted=69:2+69:3+69:4+69:floor+93:0+94:5+93:2,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`1bb55855bc7d7a3c3f9eef22fd7e235e02c3e5220a782fb29ed29a27bb69b44e`.
