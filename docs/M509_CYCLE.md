# M509-SW-REDSTONE-WIRE-FANOUT Sw redstone wire fanout

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

This milestone freezes the behavior identified by its expected signal and semantic signature.

## Qualification cycle

```text
java tools/smoke/Run.java m509-sw-redstone-wire-fanout
```

The generic runner verifies mappings and frozen inputs, executes two Worldline
and two official-oracle processes, and freezes only an exact matching trace.

## Frozen evidence

The expected signature is `6aca9f501946f63db33fca4ed618e472fead629bc7a86847355bee55802f33f0`.

Expected signal: `oracle=MATCH,fixture=m509-sw-redstone-wire-fanout,ticks=2,controlled=true`.

Frozen semantic SHA-256: `6aca9f501946f63db33fca4ed618e472fead629bc7a86847355bee55802f33f0`.
