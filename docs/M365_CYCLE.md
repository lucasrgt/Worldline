# M365 qualification cycle

`CompassPointSetCycle` rebuilds the raised-stone fixture in two fresh official
server JVMs. Each run Packet16-holds compass `345`, reads official spawn data,
observes two player cells, saves, and verifies the held item and stone fixture
through a fresh login. One official EOF may be retried after five seconds.

The client half must separately compare actual mapped and official
`TextureCompassFX` behavior under four frozen position/yaw arms.
`B173CompassPoint` only reads server spawn coordinates and player cells.

```text
java tools/smoke/CompassPointSetCycle.java m365-compass-point-set
```

Replacement signatures remain pending. Headless protocol-14 only.
