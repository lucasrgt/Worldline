# M555-TORCH-BURNOUT-SET Torch burnout set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M555 opens the official redstone-torch burnout boundary. Packet15 of
redstone torch item `76` on the north face of an unpowered raised stone
places lit wall torch `76:4`. One lever invert plus a neighbor-block update
proves the Packet53 `76 <-> 75` family. Rapid lever Packet15 toggles then
perform 24 rapid activations. The torch holds
unlit `75:4` while the support is unpowered, which is the burnt state, and
recovers to `76:4` after a 400-tick wait and later neighbor update. This
fixture does not measure an internal eight-toggle threshold or 100-tick
window. The recovered cell remains after a clean save plus fresh login.

Frozen semantic SHA-256:
`18bb8e9a083d0861b2a55ef541e55b825b6495c88e02d63298e035dad62bbc00`.

This milestone is distinct from M312's single invert (`76:4 -> 75:4` while
the support stays powered) and from M182 floor torch `76:5`. It does not
claim wire consumers, lighting, or a generic redstone model. Headless
`B173WireClient` protocol-14 only. No GUI. No Aero.

## Qualification cycle

`TorchBurnoutSetCycle` rebuilds the cloned M312 raised-stone inverter in two
fresh official server JVMs. Each run places redstone torch item `76` as live
`76:4`, proves the Packet53 `76:4 <-> 75:4` family with one invert and a
neighbor-update return to `76:4`, then performs 24 rapid lever activations.
Live unlit `75:4` holds while the support is unpowered even under a neighbor
update, then recovers to `76:4` after a 400-tick wait. The fixture does not
measure an internal toggle threshold or burnout window. Fresh login keeps
recovered `76:4`. One official EOF is
retried after a 5 second sleep.

The frozen semantic SHA-256 is
`18bb8e9a083d0861b2a55ef541e55b825b6495c88e02d63298e035dad62bbc00`.

Run directly with:

```text
java tools/smoke/TorchBurnoutSetCycle.java m555-torch-burnout-set
```

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.

Expected signal: `column=17,support=4:71:4:1:0,torch=3:72:3:76:4->75:4->76:4,burnout=75:4,recovered=76:4,rapidActivations=24,recoveryWait=400,persisted=76:4,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `18bb8e9a083d0861b2a55ef541e55b825b6495c88e02d63298e035dad62bbc00`.
