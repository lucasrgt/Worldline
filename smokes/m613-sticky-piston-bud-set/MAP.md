<!-- worldline-map-schema=1 -->
<!-- boundary=m613-sticky-piston-bud-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=c7534ac9584d6c604adde51862169dee47dad42321b5b51a6a32d7d4e354d4d4 -->

# M613 sticky piston BUD behavior map

The cloned west-facing sticky arm occupies the raised stone column.
Sticky piston `29:4` sits on the support at `(4,65,4)` with stone `1:0` in
front. Quasi-power uses a stone east-above the piston and a south lever on
that stone at `(5,66,5)`, diagonal-above the piston cell, not on the piston
and not on the M547 east-of-qc cell.

Empty-hand Packet15 of the diagonal lever QC-primes sticky `29` without
extending (`primed=29:4`). Packet15 of stone on the north face is the BUD
update (`29:4 -> 29:12`, head `34:12`). South of the piston stays air and
no adjacent dust, lever, or torch powers the piston cell. Fresh login
Packet51 keeps the pulled arm.

This map is distinct from M547 immediate sticky QC, from M548 regular
piston-`33` BUD pulse, and from shipping M144 support-lever pull. There is
no south-power fallback.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=sticky29-west+diagonal-above-lever|settle=200+10ticks|cause=packet15-north-stone-neighbor-update-not-live-direct-power|effect=official-sticky29-bud-extend+latched-unpower+pull|observation=fresh-login-packet51|column=10,primed=29:4,bud-extend=29:4->12,latched=29:12,bud-pull=29:12->4,piston=4:65:4:29:4->12->4,head=3:65:4:1:0->34:12->1:0,pushed=2:65:4:0:0->1:0->0:0,lever=5:66:5:69:3->11->3,trigger=4:65:3:1:0,south=4:65:5:0:0,direct-power=false,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`c7534ac9584d6c604adde51862169dee47dad42321b5b51a6a32d7d4e354d4d4`.
