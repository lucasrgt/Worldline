# M555 torch burnout set behavior map

Packet15 places redstone torch item `76` on the north face of an unpowered
raised stone block as lit wall torch `76:4`. Empty-hand Packet15 then inverts
that cell to unlit `75:4` and a neighbor-block update restores lit `76:4`,
proving the Packet53 `76:4 <-> 75:4` family. Rapid lever toggles then force
eight official turn-offs inside the 100-tick burnout window. After that window
the torch stays unlit `75:4` while the support is unpowered, even under a
neighbor update. A later neighbor update after the window recovers lit
`76:4`. The recovered cell survives a clean save plus fresh login.

This map is distinct from M312's single invert that remains `75:4` while the
support stays powered, and from M182 floor torch `76:5`.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+inverter+torch76-burnout|cause=packet15-item76-then-rapid-lever-toggles|wire=packet53-torch76:4<->torch75:4|oracle=live-on+burnout-off-unpowered+recover-on+fresh-login|column=17,support=4:71:4:1:0,torch=3:72:3:76:4->75:4->76:4,burnout=75:4,recovered=76:4,window=100,toggles=8,persisted=76:4,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`51a58a2129fecaba1f082e28aaa285177901ece62fb08c5e70d85fbcd3535713`.
