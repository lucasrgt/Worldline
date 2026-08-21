# TestKit example

Compile the ordinary Java 8 spec after the repository gate:

```text
$sources = Get-ChildItem examples/testkit/src/test/java/example -Filter *.java
javac --release 8 -Xlint:all,-options -Werror -classpath ".worldline/build/classes/api;.worldline/build/classes/testmodel;.worldline/build/classes/testapi" -d .worldline/examples/testkit-classes $sources.FullName
```

Copy `examples/testkit/worldline-test.properties` to the repository root or
use the explicit command after the runtime smoke has prepared the adapter:

```text
java tools/replay/Replay.java test run .worldline/examples/testkit-classes --world=.worldline/worlds/testkit-example
```

Run once with `-u` to create the demonstration snapshot. Generated classes,
worlds, results, and snapshots are caller-owned and are not release evidence.
