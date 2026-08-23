# M603 behavior map

A raised stone column hosts M307's falling-sand head bury without water or
lava. The actor stands on the southern pad. Two sand `12` blocks fall from
the eastern tower into the body cell then the head cell. Packet38 status 2
and Packet8 record suffocation `20 -> 19`. Health stays 19.

This map does not claim M465 suffocation death to Packet8 health 0, M307
compound drowning plus lava hurt, M461 fall, or M469 void. Dirt and other
normal cubes share the same in-wall path; this SET oracles sand `12`.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+falling-sand12-head|cause=stand-under-falling-sand12|wire=packet38-status2+packet8-health20->19|oracle=suffocate-hurt-not-m465-death-not-m307-compound|cause=suffocate,column=17,head=4:73:6:12:0,body=4:72:6:12:0,health=20->19,packet8=19,status=2,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`44cc512b3c80b3718cd2020ff5c519953203cef75b1a8b455fc22ed6213beae7`.
