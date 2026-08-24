<!-- worldline-map-schema=1 -->
<!-- boundary=testkit-runtime -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=d615a0d0dfba9de35405e042ad8fd12dd55a7a1e074c75dc70d1ee3afe4ab9c6 -->

# TestKit acceptance mapping

This acceptance smoke does not introduce a new Minecraft behavioral mapping.
It runs the eleven external Java 8 example specs through
`B173TestRuntimeProvider`. The descriptor declares `controlled-client-tick` as
an explicit runtime prerequisite; an isolated milestone gate prepares that
fixture when its ephemeral outputs are absent, so qualification never relies
on smoke execution order. That prerequisite independently proves the mapped
controlled client equal to the hash-pinned official JAR.

The gate covers collection, 31 test results, per-attempt sessions, block and
player observations, snapshots, hooks, table cases, artifacts, scenarios, and
the exclusive runtime lock. All generated classes, worlds, snapshots, and
results stay under `.worldline/` and are not release evidence.

The tooling acceptance signal is
`specs=11,tests=31,runtime=fresh-serial`. Its frozen SHA-256 is
`d615a0d0dfba9de35405e042ad8fd12dd55a7a1e074c75dc70d1ee3afe4ab9c6`.

This proves the deterministic TestKit runner contract only. It does not add a
Minecraft behavior claim or replace any official-JAR differential milestone.
