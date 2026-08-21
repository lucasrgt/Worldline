# M291 qualification cycle

`SpruceLeavesCycle` rebuilds the raised stone fixture in two fresh
official server JVMs. Each run places spruce log item `17` damage `1`
adjacent first, then leaves item `18` damage `1` on the top face, and
reloads leaf `18:9`. One official EOF is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`665728a20dbb06b11792f4b355f3b52d189d5cf5b8c0d06099db1447b1b7f0d5`.

Run directly with:

```text
java tools/smoke/SpruceLeavesCycle.java m291-spruce-leaves
```

Canonical evidence uses two official server JVMs and four client sessions.
