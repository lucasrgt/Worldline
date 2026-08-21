# M324 qualification cycle

`FurnaceRestSmeltsCycle` rebuilds the raised stone fixture in two fresh
official server JVMs. Each run places idle furnaces `61:2`, loads sand
`12`, cobblestone `4`, and raw fish `349` with coal `263`, and freezes live
outputs `20x1:0`, `1x1:0`, and `350x1:0` in slot 2. The result is distinct
from M296 iron/gold/pork. One official EOF is retried after a 5 second
sleep.

The frozen semantic SHA-256 is
`6c131f48c758cb5376dfd0b9504b154148f17e3295c1337c08e4c32619dc781a`.

Run directly with:

```text
java tools/smoke/FurnaceRestSmeltsCycle.java m324-furnace-rest-smelts
```

Canonical evidence uses two official server JVMs and two client sessions.
Headless protocol-14 only. No GUI. No Aero.
