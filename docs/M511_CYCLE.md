# M511-SW-REDSTONE-ORE-TRIGGER Sw redstone ore trigger

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

This milestone freezes the behavior identified by its expected signal and semantic signature.

## Qualification cycle

```text
java tools/smoke/Run.java m511-sw-redstone-ore-trigger
```

The generic differential runner verifies all mapped symbols and frozen
RetroMCP inputs, then compares two Worldline processes with two official-JAR
processes. Freeze the diagnostic signature in `smoke.properties` and rerun.

## Frozen evidence

The expected signature is `419100749c15ecc53954dc181ce9bd27403242be9ab0b60dc5cd3778a2a14419`.

Expected signal: `oracle=MATCH,fixture=m511-sw-redstone-ore-trigger,ticks=2000,controlled=true`.

Frozen semantic SHA-256: `419100749c15ecc53954dc181ce9bd27403242be9ab0b60dc5cd3778a2a14419`.
