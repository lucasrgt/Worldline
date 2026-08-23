<!-- worldline-map-schema=1 -->
<!-- boundary=m382-portal-obsidian-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=6892e4aa2cd98f329d9e6c1b83cf4feed463e1ad996fe3afe61a0a36f8778f56 -->

# M382 behavior map

One official session places fourteen obsidian `49` cells as a `4x5`
frame above a raised stone column and ignites the six interior air cells
with flint-and-steel `259`. Packet15 writes the frame; Packet15 of item
`259` on the interior support makes the official server replace those
six cells with portal `90:0`. The fourteen obsidian cells and six portal
cells survive a clean save plus fresh login in dimension `0`.

This map does not re-qualify M132 activation-as-single-behavior, M133
traversal, or M134 roundtrip travel. Headless `B173WireClient` only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|profile=allow-nether-true|fixture=stone-column+obsidian49-frame4x5+flintsteel259|construction=packet15-fourteen-obsidian49|baseline=six-air-cells|cause=packet15-flint-and-steel259|effect=official-portal-block90-six-cells|observation=live-packet53+fresh-login-packet51|oracle=fourteen-obsidian49+six-portal90+dimension-zero-no-travel|column=10,frame=4:65:4-7:69:4,obsidian=14x49,interior=6x90:0,flint=259,dimension=0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`6892e4aa2cd98f329d9e6c1b83cf4feed463e1ad996fe3afe61a0a36f8778f56`.
