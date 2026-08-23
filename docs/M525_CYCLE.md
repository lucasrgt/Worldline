# M525-SW-WIRE-CROSSING-ISOLATION Sw wire crossing isolation

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

This milestone freezes the behavior identified by its expected signal and semantic signature.

## Qualification cycle

```text
java tools/smoke/Run.java m525-sw-wire-crossing-isolation
```

The differential runner validates mappings and inputs, then compares two
Worldline executions with two official obfuscated-JAR executions.

## Frozen evidence

The expected signature is `bbd05e6a5e18bbeeda9ff5bc0b8ad3fcca475ac38719e0ce2d98047f8e91f5b5`.

Expected signal: `oracle=MATCH,fixture=m525-sw-wire-crossing-isolation,ticks=3,controlled=true`.

Frozen semantic SHA-256: `bbd05e6a5e18bbeeda9ff5bc0b8ad3fcca475ac38719e0ce2d98047f8e91f5b5`.
