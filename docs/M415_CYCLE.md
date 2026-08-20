# M415 qualification cycle

`WaterCobbleSetCycle` rebuilds two raised stone trenches in two fresh
official server JVMs. Each run seeds still lava `11`, opens dirt gates onto
air, waits for official horizontal flowing lava (moving block `10`,
Packet53 `11:2`), places still water `9` beside those flowed cells, and
reloads cobble `4` at both meeting cells. The signal must name flowing lava
`10`, water `8/9`, and cobble `4`. One official EOF is retried after a 5
second sleep. Headless `B173WireClient` is the only client. There is no GUI
and no Aero path.

Run directly with:

```text
java tools/smoke/WaterCobbleSetCycle.java m415-water-cobble-set
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`bf5ec9eaf7f4f9ec7cf8652c8bdef0af40a1d8fa89b618d519dc571fddc66148`.
