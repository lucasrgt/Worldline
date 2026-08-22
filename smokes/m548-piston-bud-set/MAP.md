# M548 behavior map

The cloned M367 west-facing piston occupies one raised stone column.
Normal piston `33:4` sits on the support at `(4,65,4)` with stone `1:0` in
front. There is no lever and no QC power remaining on.

Empty-hand inventory slot `2` is redstone torch item `76`. Packet15 places
that torch on the payload. The official server starts one west extension as
moving piston `36:4`, then self-clears: base `33:4`, head air, payload left
at `(2,65,4)`, torch gone. Fresh login Packet51 keeps that retracted pulse
result.

This map is distinct from M367 lever-power (`69` Packet15 extend/retract)
and from M546 QC-with-power-above remaining on (`33:12` held).

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=piston33-west+payload-stone|settle=200+10ticks|cause=packet15-neighbor-torch76-on-payload|effect=official-piston33-bud-pulse-not-lever-not-qc-hold|observation=fresh-login-packet51|column=10,bud-pulse=33:4->36:4->33:4,piston=4:65:4:33:4->36:4->33:4,head=3:65:4:1:0->0:0->0:0,pushed=2:65:4:0:0->1:0,torch=3:66:4:0:0,power=none,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`64edc418a23140583ce5015dead697010582f99862cc5e19d6e8e7e53f02bcff`.
