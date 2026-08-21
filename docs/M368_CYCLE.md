# M368 qualification cycle

`MoreDyeWoolCraftsCycle` rebuilds the white-wool plus yellow, orange, and
pink dye fixture in two fresh official server JVMs. Each run crafts three
new dyed wool damages in the personal 2x2 grid and reloads the colored
stacks. One official EOF is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`af97665c706b12d71c1c228a931a7efec0c18fda505b259de31fdf174b8a17b9`.

Run directly with:

```text
java tools/smoke/MoreDyeWoolCraftsCycle.java m368-more-dye-wool-crafts
```

Canonical evidence uses two official server JVMs and four client sessions.
