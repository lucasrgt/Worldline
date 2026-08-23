<!-- worldline-map-schema=1 -->
<!-- boundary=m144-sticky-piston-pull -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=a56517b95b971f64b951329f03267a3c65259a557dc00925e24e3f9235fe377a -->

# M144 behavior map

The M142/M143 geometry is rebuilt with sticky piston `29`. Its west-facing
extended state is base `29:12`, head `34:12`, and stone `1:0` two cells west.
Those values are reloaded before the treatment.

Lever deactivation produces `69:9→69:1`, `29:12→29:4`,
`34:12→1:0`, and displaced stone `1:0→0:0`. The raised-volume digest contains
exactly those four cells and a final independent Packet51 contains the pulled
stone.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=fresh-extended-sticky-piston29+head34:12+displaced-stone1|settle=200+10+save+fresh+10ticks|cause=packet15-lever-deactivate|effect=official-sticky-retract+stone-pullback|observation=fresh-login-packet51|column=10,lever=5:64:4:9->1,piston=4:65:4:12->4,head=34:12->1:0,pushed=1:0->0:0,raised-states=4:dd46ed264a8e23b1096a5e0bbbcc78241a437f65b7cdf46bfcd49af1abe0be85|disconnect=clean
```

Frozen semantic SHA-256:
`a56517b95b971f64b951329f03267a3c65259a557dc00925e24e3f9235fe377a`.
