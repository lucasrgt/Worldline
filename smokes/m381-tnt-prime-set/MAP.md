# M381 behavior map

Packet15 places TNT item `46` on a raised stone column. Packet15 of
flint-and-steel item `259` on that TNT cell removes block `46:0` into
a primed entity. Packet23 type `50` is the primed-TNT object on the
existing tracker. After the bounded fuse, Packet60 strength `4`
destroys the constructed support. The TNT cell and support remain air
after a clean save plus fresh login.

This is distinct from M219 unprimed place, M137 Packet60 detonate
without Packet23 type `50`, and M343 flint-and-steel fire. Exact blast
rays and destroyed-cell count are not hashed.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+tnt46+flint259|cause=packet15-item46+packet15-item259-prime|fuse=100ticks|wire=packet23-type50+packet60-center+strength+relative-destroyed-cells|oracle=live-prime-object+crater-support-air+fresh-login|column=15,support=4:69:4:1:0->0:0,tnt=4:70:4:46:0->0:0,flint=259,packet23=50,strength=4,crater=true,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`6cf1cfe074d14a3c856cf768c9a8b9cdc9cfa573b8ee2e901445db31692bfad5`.
