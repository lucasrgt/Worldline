# M467 behavior map

The fixture raises an isolated `7×7` grass platform and places default
mob spawner `52:0`. After a clean save the region NBT `EntityId` is
rewritten from `Pig` to `Zombie`. One smoke boots Easy (`difficulty=1`)
then Hard (`difficulty=3`), reseeding Health `20` between those boots.
Console time `14000` lets Packet24 type `54` spawn; unarmored melee must
publish Packet38 status `2` then Packet8.

Official dedicated-server `spawn-monsters=true` stores world difficulty
`1`, so both property boots freeze Easy-scale zombie melee `5/3+1=2`
as Packet8 `20 -> 18`. Hard's `*3/2` branch is not taken. This is not
armor reduction (M451), peaceful despawn (M454), or door break (M446).

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-7x7-grass-platform+spawner52-zombie|cause=nbt-entityid-zombie+time-14000+difficulty-1-then-3|wire=packet24-type54+packet38-status2+packet8-easy-then-hard|oracle=difficulty-property-zombie-melee-not-armor-not-peaceful-not-door|column=17,support=4:71:4:1:0,platform=7x7-48grass,mob=type54,difficulty=1+3,easy=20->18,hard=20->18,delta=2+2,armor=none,night=14000,heal=health20,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`61e1ac15b1e84c70af6ec58f615e81db3d5a6ae0c3deaac931da803a16f459d7`.
