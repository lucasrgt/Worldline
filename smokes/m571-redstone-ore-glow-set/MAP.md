# M571 behavior map

Packet15 places unlit redstone ore `73:0` as an east-floor cell beside a
raised stone column. Empty-hand Packet15 clicks that cell to glowing
`74:0`. Official random ticks then convert it back to `73:0`. Fresh login
Packet51 observes the darkened cell because the live Packet53 fade can be
masked by lighting chunk resends. Walking onto the darkened cell lights it
again; a second save plus login keeps unlit `73:0`.

This map does not claim M229 unlit placement as the glow boundary, M511-SW
controlled trigger membership, redstone-dust drops, or scheduled updates.
The trigger schedules nothing; glowing ore `74` is marked for random ticks.
Exact fade wait length is not hashed.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+ore73-east-floor+west-perch|cause=empty-hand-packet15-activate+moveAndObserve-on-ore+random-ticks|wire=packet53-ore73:0->74:0->73:0|oracle=click-glow+step-glow+random-tick-darken+fresh-login-unlit73:0|column=17,support=4:71:4:1:0,perch=3:71:4:1:0,ore=5:71:4:73:0,click=73:0->74:0,click-dark=74:0->73:0,step=73:0->74:0,step-dark=74:0->73:0,persisted=true,clients=3,disconnect=clean
```

Frozen semantic SHA-256:
`0f6e8216f809dfc39632b18769fef8f62e2c12e4481958e5f135b515484f8098`.
