# TestKit example

The supported-version matrix and copy-ready CI workflow are in
[`docs/CONSUMING_WORLDLINE_TESTKIT.md`](../../docs/CONSUMING_WORLDLINE_TESTKIT.md)
and [`ci/worldline-test.yml`](ci/worldline-test.yml).

Validate the checkout and compile the ordinary Java 8 specs:

```text
java tools/harness/Gate.java
$sources = Get-ChildItem examples/testkit/src/test/java/example -Filter *.java
javac --release 8 -Xlint:all,-options -Werror -classpath ".worldline/build/classes/api;.worldline/build/classes/testmodel;.worldline/build/classes/testapi" -d .worldline/examples/testkit-classes $sources.FullName
```

Copy `examples/testkit/worldline-test.properties` to the repository root or
use the explicit command after the runtime smoke has prepared the adapter:

```text
java tools/replay/Replay.java test run .worldline/examples/testkit-classes --world=.worldline/worlds/testkit-example
```

`VanillaBehaviorSpec` demonstrates `WorldlineBehavior`, mod evidence, and an
exact frozen vanilla expectation. The packaged 0.3.0 runner can list every
public identity without starting Minecraft:

```text
java -jar worldline-test-runner-0.3.0.jar behaviors list
```

Run once with `-u` to create the demonstration snapshot. Generated classes,
worlds, results, and snapshots are caller-owned and are not release evidence.
