# M149 behavior map

One official default spawner creates a pig identity shared by two peers. The
actor then sends Packet7 with a diamond sword against that identity. Both peers
independently observe Packet38 status 2, Packet38 status 3 and Packet29 destroy
for the same entity.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-7x7-grass-platform+default-spawner52|cause=official-diamond-sword-packet7|wire=packet38-status3+packet29-destroy|oracle=two-peer-identical-mob-death|column=17,platform=7x7-48grass,spawner=52:0,mob=type90+shared-id,death=packet7-sword276+packet38-status3+packet29+hurt2,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`c189244beb58382402de4313f9d6be75c90f398e404a7df2ebbbdfa8b34c5048`.
