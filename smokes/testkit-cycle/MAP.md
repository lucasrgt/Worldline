# TestKit acceptance mapping

This acceptance smoke does not introduce a new Minecraft behavioral mapping.
It runs the ten external Java 8 example specs through
`B173TestRuntimeProvider` after `controlled-client-tick` has independently
proved the mapped controlled client equal to the hash-pinned official JAR.

The gate covers collection, 30 test results, per-attempt sessions, block and
player observations, snapshots, hooks, table cases, artifacts, scenarios, and
the exclusive runtime lock. All generated classes, worlds, snapshots, and
results stay under `.worldline/` and are not release evidence.

The tooling acceptance signal is
`specs=10,tests=30,runtime=fresh-serial`. Its frozen SHA-256 is
`ae3c579924d95e39b28c676c6776a4a9af7303f7b8f9a771207734462bee369a`.

This proves the deterministic TestKit runner contract only. It does not add a
Minecraft behavior claim or replace any official-JAR differential milestone.
