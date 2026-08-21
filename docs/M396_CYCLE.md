# M396 qualification cycle

`RemainingWoolCraftsCycle` rebuilds the white-wool plus magenta, light
blue, and lime dye fixture in two fresh official server JVMs. Each run
crafts three remaining dyed wool damages in the personal 2x2 grid and
reloads the colored stacks. One official EOF is retried after a 5 second
sleep.

The frozen semantic SHA-256 is
`7bd1423c0f7af5c289a638d55eb9b16ec8b709217f849b00e95b0a3316990c54`.

Run directly with:

```text
java tools/smoke/RemainingWoolCraftsCycle.java m396-remaining-wool-crafts
```

Canonical evidence uses two official server JVMs and four client sessions.
