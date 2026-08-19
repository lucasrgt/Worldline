# M150 behavior map

One official default spawner creates a pig identity shared by two peers. After
the adapter-owned identity records a nonzero horizontal Packet31/33/34
transition, the actor sends Packet7 without a caller-supplied entity id. Both
peers observe Packet38 status 3, Packet29 destroy and at least one Packet21
raw porkchop `319`.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-7x7-grass-platform+default-spawner52|cause=official-diamond-sword-packet7|wire=packet38-status3+packet29+packet21-pork319|oracle=two-peer-identical-observed-pork-drop|column=17,platform=7x7-48grass,spawner=52:0,mob=type90+shared-id,movement=observed-horizontal,death=observed-packet7+packet38-status3+packet29,drop=pork319+packet21+shared-id,kills<=8,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`90cf54607ffd52b403765121c14d821e80e9996702f158c29efe63aee15b0d33`.
