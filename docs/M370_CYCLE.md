# M370 qualification cycle

`RemainingFurnaceSmeltsCycle` rebuilds the raised stone fixture in two fresh
official server JVMs. Each run places idle furnaces `61:2`, loads cactus `81`,
oak log `17`, and clay ball `337` with coal `263`, and freezes live outputs
`351x1:2`, `263x1:1`, and `336x1:0` in slot 2. The result is distinct from
M296 iron/gold/pork and M324 sand/cobble/fish. One official EOF is retried
after a 5 second sleep.

The frozen semantic SHA-256 is
`912452d315840ced68811ccce77f3cde4f1250eac7068c5ddd9f85e22a607a2a`.

Run directly with:

```text
java tools/smoke/RemainingFurnaceSmeltsCycle.java m370-remaining-furnace-smelts
```

Canonical evidence uses two official server JVMs and two client sessions.
Headless protocol-14 only. No GUI. No Aero.
