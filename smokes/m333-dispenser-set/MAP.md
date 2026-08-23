<!-- worldline-map-schema=1 -->
<!-- boundary=m333-dispenser-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=46b62a083dad7f0e54a72e16e9b51144add22acb4cb53b75b51439b04385894e -->

# M333 behavior map

Packet15 places dispenser item `23` on a raised stone column as west-facing
`23:4`. Packet102 loads cobblestone `4` then oak planks `5` into the Trap
window. Two side-lever rising edges eject both stacks as Packet21. This is
distinct from M231 place-only facing persistence.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-dispenser23-west+side-lever69|cause=packet15-item23+packet102-load-4+5+packet15-lever-activate|wire=packet53-dispenser23+packet21-4+5|oracle=official-dispenser-set-eject|column=17,disp=4:72:4:23:4,lever=5:71:4:1->9,load=4+5,ejects=4+5,drop=packet21-4+5,remain=empty,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`46b62a083dad7f0e54a72e16e9b51144add22acb4cb53b75b51439b04385894e`.
