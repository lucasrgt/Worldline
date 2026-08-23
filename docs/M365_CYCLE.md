# M365 qualification cycle

`CompassPointSetCycle` rebuilds the raised-stone fixture in two fresh official
server JVMs. Each run Packet16-holds compass `345`, reads official spawn data,
observes two player cells, saves, and verifies the held item and stone fixture
through a fresh login. One official EOF may be retried after five seconds.

The client half compares actual mapped and official `TextureCompassFX`
behavior under four frozen position/yaw arms.
`B173CompassPoint` only reads server spawn coordinates and player cells.

```text
java tools/harness/Gate.java --milestone m365-compass-point-set
java tools/harness/Gate.java --milestone controlled-client-tick
```

The server signature is
`45694ff4da19a7b3c554d405380e9535c075d7fee1f3880674813ed5cc9a8b68`.
The shared client-physics signature is
`c2508b3dfff5f7852ce6b3155c5257ba781482031001cfdd38326b3363a5c014`.
Headless protocol-14 only.
