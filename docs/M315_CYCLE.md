# M315 qualification cycle

`DyeWoolCraftsCycle` rebuilds the white-wool plus dye fixture in two fresh
official server JVMs. Each run crafts three dyed wool damages in the
personal 2x2 grid and reloads the colored stacks. One official EOF is
retried after a 5 second sleep.

The frozen semantic SHA-256 is
`f9b78bfc4331c0fc5e92dd33443743a0c9b46e17815f9f89b20fd2535c2405d2`.

Run directly with:

```text
java tools/smoke/DyeWoolCraftsCycle.java m315-dye-wool-crafts
```

Canonical evidence uses two official server JVMs and four client sessions.
