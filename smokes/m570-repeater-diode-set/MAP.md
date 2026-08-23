# M570-REPEATER-DIODE-SET repeater diode set behavior map

The cloned M170 west-facing repeater occupies one raised stone column.
Unpowered repeater `93:3` sits on the support at `(4,72,4)` with input
dust `55` east and output dust `55` west. South-face levers `69:3` sit
on the east and west pads so each lever can power only one dust cell.

Empty-hand Packet15 on the output-side lever powers output dust while
the repeater stays `93:3` and input dust stays `55:0`. Packet15 on the
input-side lever then conducts `93:3 -> 94:3` with both dust cells at
`55:15`. Fresh login Packet51 keeps powered `94:3`.

This map is distinct from M170 place/power and M341 delay-tune. It does
not claim locking, BUD, or a generic redstone model.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-west-repeater93+in-dust55+out-dust55+south-levers|settle=20+40ticks|cause=packet15-reverse-lever-then-forward-lever|effect=official-repeater-diode-forward-conduction+reverse-isolation|observation=fresh-login-packet51|column=17,support=4:71:4:1:0,repeater=4:72:4:93:3->94:3,facing=3,delay=1,reverse=rpt=93:3+in=55:0+out=55:15,forward=rpt=94:3+in=55:15+out=55:15,isolated=true,persisted=94:3,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`82681454d27796440d2c56cc8d0a67ef9f43084871bd0b155424f9b01f827c90`.
