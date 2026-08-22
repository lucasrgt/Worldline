# M550 behavior map

Packet15 places dispenser item `23` on a raised stone column as west-facing
`23:4`. Packet102 loads cobblestone `4` into the Trap window. An east tower
places stone `1:0` on the dispenser without clicking it, then a floor lever
on that block above. The dispenser cell has no side lever. One rising edge
ejects Packet21 cobble. Floor-lever facing 5/6 is not frozen; only the
powered bit `0->8` is. This is official QC, not M153/M333 adjacent-power
eject.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-dispenser23-west+qc-stone-above+top-lever69|cause=packet15-item23+packet102-load-4+packet15-qc-lever-activate|wire=packet53-dispenser23+packet21-4|oracle=official-dispenser-qc-eject|column=17,disp=4:72:4:23:4,qc=4:73:4:1:0,lever=4:74:4:floor:0->8,load=4x1,drop=packet21-4x1,remain=empty,power=qc-above,adjacent=none,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`fbebc71b9da63b30d1c347778fb92cafea108114e52b79af79ae955487ef73db`.
