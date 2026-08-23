# M594 netherrack fire persist behavior map

An isolated Overworld stone column places netherrack `87` with flint-and-steel
fire `51` in the air cell above. Planks `5` sit on an east pedestal, also
ignited. After a 40-tick live check the actor holds 2400 ticks away from the
flame. A fresh login must still see netherrack fire `51` while both plank-side
cells are no longer fire.

This map does not claim stone-support ignition (M268), fire-support collapse
(M515), wool consumption (M152/M343), or adjacent spread (M413).

Frozen signal:
`column=17,support=4:71:4:1:0,rack=4:72:4:87:0,flint=259,nether-fire=4:73:4:51,planks=5:73:4:5,plank-fire=5:74:4:expired,hold=2400,netherrack-persist=true,clients=2,disconnect=clean`

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-netherrack87+planks5+flintsteel259|cause=packet15-item259+long-observation-window|wire=packet53-fire51-netherrack-persist+plank-fire-expired|oracle=netherrack-fire-persist-not-stone-ignition-not-support-extinguish|column=17,support=4:71:4:1:0,rack=4:72:4:87:0,flint=259,nether-fire=4:73:4:51,planks=5:73:4:5,plank-fire=5:74:4:expired,hold=2400,netherrack-persist=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`4ae2baf1dfa018b12ca7517f9660064ac936c01f90c9a76e84a3b25da95e8683`.
