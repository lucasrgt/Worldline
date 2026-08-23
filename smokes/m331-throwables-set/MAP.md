<!-- worldline-map-schema=1 -->
<!-- boundary=m331-throwables-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=63d18b0a65f745ad18fa9a7a9e8e345e8bffe83e067224ff8687c1b03c0a7328 -->

# M331 behavior map

The fixture raises an isolated stone column. One session air-uses snowball
item `332`, egg item `344`, and fishing rod item `346` through Packet15
direction `255` while looking forward. Those three uses emit official
Packet23 types `61`, `62`, and `90` on the existing object tracker. Two
headless peers observe the same entity identity, type, thrower, and pose
for each spawn. Catch RNG and egg-hatch RNG are not hashed.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+snowball332+egg344+rod346|cause=packet15-dir255-item332+item344+item346|wire=packet23-type61+type62+type90|oracle=two-peer-identical-throwable-objects|column=17,support=4:71:4:1:0,snow=type61+shared-id+thrower0,egg=type62+shared-id+thrower0,hook=type90+shared-id+thrower0,items=332+344+346,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`63d18b0a65f745ad18fa9a7a9e8e345e8bffe83e067224ff8687c1b03c0a7328`.
