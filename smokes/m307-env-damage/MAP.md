# M307 environmental damage map

A raised stone column hosts M299's two-deep still-water drown, M330's
falling-sand head bury, then M301's still lava `11:0`. Packet38 status 2 and
Packet8 record drowning `20 -> 18`, suffocation `19 -> 18`, then lava
`19 -> 15`. Health 15 survives a clean save plus fresh login.

This is not a single-cause milestone. The frozen signal names drowning,
suffocation, and lava together.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+two-still-water+falling-sand12+still-lava11|cause=submerged-eye-air-deplete+stand-under-falling-sand12+stand-in-lava|wire=packet38-status2+packet8-health20->18/19->18->15|oracle=drown+suffocate+lava-drops+fresh-login|causes=drown+suffocate+lava,column=17,water=4:72:4+4:73:4,head=4:73:6:12:0,lava=4:72:2:11:0,health=20->18->19->18->15,status=2,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`8a51289b35f57567a0dfbc0f3cf8f1d6981dac6219b52d494aac34f56713cba7`.
