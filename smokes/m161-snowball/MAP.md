# M161 behavior map

Official b1.7.3 server JAR class `be` is EntitySnowball, registered as
`Snowball`. `cf` is ItemSnowball shifted index `332`. EntityTrackerEntry `ff`
emits Packet23 class `ls` with object type `61`. Packet `ls` is entity int,
type byte, three fixed-point ints and thrower int; velocity shorts follow only
when thrower is positive. Snowball uses thrower `0`, so the live payload is
exactly 21 bytes. Packet15 direction `255` is the sole NetServerHandler `ha`
path into ItemSnowball right-click.

Two connected clients then require one identical official type-`61` packet,
positive non-player identity, thrower zero and spawn pose beside the actor.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+snowball332|cause=packet15-direction255-item332|wire=packet23-int+byte+3int+throwerInt|oracle=two-peer-identical-snowball61-packet|column=17,object=type61+shared-positive-id+thrower0+near-actor,item=332,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`1865bda95354d11a5a95ac4eaaf3fb8ad521e4b6195e6c651715e6955e7a149d`.
