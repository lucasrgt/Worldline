# M604 drowning set behavior map

A raised stone column hosts two-deep still water `8/9` over the eye. The
official air meter empties while `Drown604` stays fully submerged. Packet38
status 2 precedes Packet8 health `20 -> 18` (damage 2). Health stays 18.

This map does not claim M465 drowning death to Packet8 health 0, M307
compound suffocation plus lava hurt, M461 fall, or M469 void.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+two-still-water|cause=submerged-eye-air-deplete|wire=packet38-status2+packet8-health20->18|oracle=drowning-hurt-not-m465-death-not-m307-compound|cause=drown,column=17,water=4:72:4+4:73:4,health=20->18,damage=2,status=2,packet8=18,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`be6df540130ff955f1e6aa69fc938d51bcf8c9f3e219730af9ff0207ee88bc5d`.
