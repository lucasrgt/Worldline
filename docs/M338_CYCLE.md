# M338 qualification cycle

`FurnaceFuelSetCycle` rebuilds the raised stone fixture in two fresh official
server JVMs. Each run places idle furnaces `61:2`, loads cobble `4` with coal
`263`, oak planks `5`, and lava bucket `327`, and freezes live stone `1x1:0`
in slot 2 together with Packet105 burn/progress for all three fuels. The
result is distinct from M296 ingot and pork outputs. One official EOF is
retried after a 5 second sleep.

The frozen semantic SHA-256 is
`d412ed91eacea33e26daaf3f37c6494ecb462ee19694093f7126187f36a2b957`.

Run directly with:

```text
java tools/smoke/FurnaceFuelSetCycle.java m338-furnace-fuel-set
```

Canonical evidence uses two official server JVMs and two client sessions.
Headless protocol-14 only. No GUI. No Aero.
