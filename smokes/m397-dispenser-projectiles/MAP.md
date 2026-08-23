<!-- worldline-map-schema=1 -->
<!-- boundary=m397-dispenser-projectiles -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=66d497bee36abdc673c44336dad9a75afcc08fcf7ade36676c652023100b1731 -->

# M397 behavior map

Packet15 places dispenser item `23` on a raised stone column as west-facing
`23:4`. Packet102 loads snowball `332` then egg `344` into the Trap window.
Two side-lever rising edges eject both stacks as Packet23 types `61` and
`62` on the existing object tracker. Distinct from M333 Packet21 item
drops and from M331 player air-use throwables.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-dispenser23-west+side-lever69+snowball332+egg344|cause=packet15-item23+packet102-load-332+344+packet15-lever-activate|wire=packet53-dispenser23+packet23-type61+type62|oracle=official-dispenser-projectile-set|column=17,disp=4:72:4:23:4,lever=5:71:4:1->9,load=332+344,snow=type61+thrower0,egg=type62+thrower0,remain=empty,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`66d497bee36abdc673c44336dad9a75afcc08fcf7ade36676c652023100b1731`.
