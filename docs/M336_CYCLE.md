# M336 qualification cycle

`SlabMetaCraftsCycle` rebuilds the workbench fixture in two fresh official
server JVMs. Each run crafts sandstone slab `44:1`, wood slab `44:2`, and
cobble slab `44:3` from their vanilla recipes and reloads those stacks
after save plus fresh login. One official EOF is retried after a 5 second
sleep.

The frozen semantic SHA-256 is
`e75d0b2bb489e7ea157ad321c8dc141c57039e5411b10b96658868f3b231cc57`.

Run directly with:

```text
java tools/smoke/SlabMetaCraftsCycle.java m336-slab-meta-crafts
```

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
