# M522-SW qualification cycle

Run under the exclusive official-runtime lock:

```text
java tools/smoke/PersonalSlotSwapCycle.java m522-sw-personal-slot-swap
```

The cycle verifies the official server JAR, compiles the adapter plus smoke,
and compares two fresh server scenarios. Freeze the diagnostic signature in
`smoke.properties`, then rerun the same cycle to qualify it.
