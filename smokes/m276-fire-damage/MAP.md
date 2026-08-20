# M276 behavior map

Official flint and steel Packet15 ignites air above netherrack `87:0` as fire
`51:0`. The headless actor then `moveAndObserve`s into that flame cell so the
official server emits Packet8 health `20 -> 19`. Stepping off the flame keeps
health 19, which survives a clean save plus fresh login.

This is not M151 flame persistence and not M152 wool consumption.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-netherrack87+flame51|cause=packet15-flint-and-steel259+stand-in-fire|wire=packet53-fire51+packet8-health20->19|oracle=live-fire-damage+fresh-login-health19|column=17,rack=4:72:4:87:0,fire=4:73:4:51:0,health=20->19,damage=1,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`6df6fefaf368f9cde54b95ead0d046469348c56c0bc386f4306b0d0a5a14a043`.
