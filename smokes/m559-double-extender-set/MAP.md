<!-- worldline-map-schema=1 -->
<!-- boundary=m559-double-extender-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=49d44fb82433fdcf9dcf8ca5201aa946b783e9d3539c9689d6a2284af36fac0f -->

# M559 behavior map

The cloned M142 west-facing piston family occupies one raised stone column.
Sticky piston `29:4` sits on the support at `(4,65,4)`. Regular piston `33:4`
occupies its west cell `(3,65,4)`, and cobble `4:0` occupies `(2,65,4)`. Side
levers sequence the two arms: rear lever `69:1` on the support, front lever
`69:3` on the pad under the post-extend regular piston.

Empty-hand Packet15 on the rear lever extends sticky `29` (`29:4 -> 29:12`,
head `34:12`) and pushes both the regular piston and cobble one cell west.
A second Packet15 on the front lever then extends piston `33` (`33:4 -> 33:12`,
head `34:4`) so cobble travels a second cell to `(0,65,4)`. Fresh login
Packet51 keeps the extended double-extender chain.

This map is distinct from M145 two-material payload on one piston (one-cell
shift `1:0→34:4 / 4:0→1:0 / 0:0→4:0`) and from M147 twelve-block capacity.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=sticky29-west+piston33-west+payload-cobble4+sequenced-levers|settle=200+10ticks|cause=packet15-rear-lever-then-front-lever|effect=official-double-extender-two-cell-payload|observation=fresh-login-packet51|column=10,cells=2,sequenced=29-then-33,rear=29:4->12,front=33:4->12,rear-cell=4:65:4:29:12,front-from=3:65:4,front-to=2:65:4:33:12,payload=2:65:4->1:65:4->0:65:4:4:0,sticky-head=3:65:4:34:12,piston-head=1:65:4:34:4,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`49d44fb82433fdcf9dcf8ca5201aa946b783e9d3539c9689d6a2284af36fac0f`.
