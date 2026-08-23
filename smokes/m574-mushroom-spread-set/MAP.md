# M574 behavior map

Packet15 of brown mushroom item `39` and red mushroom item `40` builds a
roofed 7x7 raised-stone pad: striped sources, dark opaque air samples, and
one glass `20` floor cell. Official random ticks then write Packet53 air to
brown `39` or red `40` on at least one dark opaque sample. Air above glass
stays empty. Exact wait length and which dark sample converts are not
hashed.

This map does not claim M200/M201/M383 mushroom place as the conversion,
huge mushrooms, or bone-meal.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+dark-7x7-pad+mushroom39+mushroom40+glass20|cause=packet15-item39+item40+random-ticks|wire=packet53-air-to-mushroom39/40+glass-air|oracle=dark-opaque-spread+glass-stay+fresh-login|column=17,support=4:71:4:1:0,pad=7x7,sources=15,brown=9,red=6,targets=9,glass=4:71:5:20:0,roof=4:73:4:1:0,spread=air->39/40,glass-stay=true,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`c811235b7974b4ef624d19676213d5795b8b284eb89feb64110d5dc20703b076`.
