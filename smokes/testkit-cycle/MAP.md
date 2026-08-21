# TestKit acceptance mapping

This acceptance smoke does not introduce a new Minecraft behavioral mapping.
It runs the ten external Java 8 example specs through
`B173TestRuntimeProvider` after `controlled-client-tick` has independently
proved the mapped controlled client equal to the hash-pinned official JAR.

The gate covers collection, 30 test results, per-attempt sessions, block and
player observations, snapshots, hooks, table cases, artifacts, scenarios, and
the exclusive runtime lock. All generated classes, worlds, snapshots, and
results stay under `.worldline/` and are not release evidence.
