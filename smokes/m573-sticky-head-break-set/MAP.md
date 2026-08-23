<!-- worldline-map-schema=1 -->
<!-- boundary=m573-sticky-head-break-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=eb5e939b57ac0f6890a4d5bbb17ff700d690d6f8d3384748b4e3afd1ad0e0869 -->

# M573 behavior map

The cloned M144 west-facing sticky arm occupies one raised stone column.
Sticky piston `29:4` sits on the support at `(4,65,4)` with stone `1:0` in
front and a side lever `69:1`. Empty-hand Packet15 extends sticky piston
`29` (`29:4 -> 29:12`, sticky head `34:12`, displaced stone). Packet14
while holding iron pickaxe `257` then fully breaks the extended HEAD
`34`. Official leftover cleanup removes sticky base `29` to air and drops
Packet21 sticky piston `29:1:0`. The head cell is air. The lever stays
powered `69:9`. Fresh login Packet51 keeps those leftover cells.

This map is distinct from M554 extended-base break of regular piston `33`
and from M144 retract-by-unpower (`29:12 -> 29:4` with stone pullback).

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=sticky29-west-extended|settle=200+20ticks|cause=packet15-lever-activate+packet14-ironpick257-head34|effect=official-extended-sticky-head-break+base29-removed|observation=fresh-login-packet51|column=10,extend=29:4->12,head-break=34:12->0,base-left=29:12->0,piston=4:65:4:29:4->12->0,head=3:65:4:1:0->34:12->0:0,pushed=2:65:4:0:0->1:0->1:0,lever=5:64:4:69:1->9,drops=packet21-29,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`eb5e939b57ac0f6890a4d5bbb17ff700d690d6f8d3384748b4e3afd1ad0e0869`.
