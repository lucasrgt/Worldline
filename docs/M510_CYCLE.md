# M510-SW-REDSTONE-WIRE-LOOP-RECOVERY Sw redstone wire loop recovery

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

This milestone freezes the behavior identified by its expected signal and semantic signature.

## Qualification cycle

```text
java tools/smoke/Run.java m510-sw-redstone-wire-loop-recovery
```

The differential runner validates mappings and frozen inputs, then requires
identical traces from two Worldline and two official-JAR processes.

## Frozen evidence

The expected signature is `5e7146f6ad6016166d3ecbe31d2d334bbc4aedabe1131c26ab335399743a373b`.

Expected signal: `oracle=MATCH,fixture=m510-sw-redstone-wire-loop-recovery,ticks=2,controlled=true`.

Frozen semantic SHA-256: `5e7146f6ad6016166d3ecbe31d2d334bbc4aedabe1131c26ab335399743a373b`.
