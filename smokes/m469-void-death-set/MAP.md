# M469 behavior map

An empty player is written at `4.5, -8.5, 4.5` so login pose `y` is below
0 and still above the official `-64` kill plane. Packet13 walks down in
steps of at most 9 until `y <= -64` while health stays positive. Bounded
Packet10 heartbeats then let vanilla void damage produce Packet8 health
`0`. `respawn()` writes Packet9 for dimension `0` and waits for health
`20` with spawn `y >= 0`.

This map does not re-qualify M135's wait already under the kill plane,
M135 respawn from mob or lava, M461 fall damage above the void, or M465
drowning, suffocation, or lava. Headless `B173WireClient` only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=underside-void-air-above-kill|cause=packet13-walk-down-cap9-until-pose-y<0|wire=packet8-health20->0+packet9-dimension-zero|oracle=void-walk-death-not-fall-not-env-not-m135-wait-under-kill|walk-off=cap9,steps=7,pose-y<0,health=20->0->20,packet8=0,packet9=09:00,dimension=0,spawn-y>=0,persisted=20,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`52332cdbcd2108c4f8baa59811bffe40d9ba676283c851371bb2bee321f7ef98`.
