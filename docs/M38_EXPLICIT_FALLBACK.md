# M38 Explicit Movement Fallback

M38 adds immutable `MovementAlternative` pairs to the recovering route session.
Each pair contains one primary and one caller-supplied fallback. The primary is
observed exactly once. An unchallenged result skips its fallback; a corrected
result executes the fallback exactly once. At most 32 pairs are accepted so the
existing 64-outcome route bound cannot be exceeded.

The official smoke supplies a safe primary with a deliberately detectable Z
fallback, then a solid-collision primary with an X fallback. The exact outcomes
must be `UNCHALLENGED`, `CORRECTED`, `UNCHALLENGED`; unchanged final Z proves the
safe fallback was skipped. The cache remains loaded and player NBT must persist
the corrected primary's explicit fallback pose on two fresh servers.

## Non-claims

M38 does not discover a fallback, retry a primary, choose among alternatives,
infer correction causes, execute asynchronously, or control server ticks.
