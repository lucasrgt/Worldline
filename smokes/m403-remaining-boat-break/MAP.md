<!-- worldline-map-schema=1 -->
<!-- boundary=m403-remaining-boat-break -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=34eb6766ee9194e30d2efd5712a5e932110351176e336e526d7c6f23a877dedc -->

# M403 behavior map

One official session places two boat items `333` through Packet15 direction
255 on the M154 still-water cell `4:60:4:9:0`. Two peers decode each
protocol-14 Packet23 type `1` at quantized pose `144:1993:144`. Empty-hand
Packet7 button `1` attacks each shared boat until official Packet21 wreckage
arrives: plank `5` from both boats plus stick `280`. Official boat-break
does not return boat item `333`.

This map does not re-qualify shipping spawn-only (M154), craft-only (M326),
or ride-then-detach (M378). Headless `B173WireClient` only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=natural-water9+boat333x2|cause=packet15-dir255-boat333+empty-hand-packet7-button1-attack|wire=packet23-type1+packet7-button1+packet21-5+packet21-280|oracle=two-peer-two-boat-breaks-not-spawn-only-not-craft-not-ride|water=4:60:4:9:0,boats=2,boat=type1+shared-id+packet23+packet7-button1,item=333,drop=packet21-5+packet21-5+packet21-280,pose=144:1993:144,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`34eb6766ee9194e30d2efd5712a5e932110351176e336e526d7c6f23a877dedc`.
