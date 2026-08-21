# M297 qualification cycle

`BasicCraftsCycle` rebuilds one official personal 2x2 basic-crafts SET in two
fresh official server JVMs. Each run seeds log `17` and coal `263`, crafts
planks `5x4`, sticks `280x4`, and torches `50x4`, and reloads the stored
stacks after save plus a fresh login. One official EOF is retried after a 5
second sleep.

The frozen semantic SHA-256 is
`f62ec64a6ea2c9990cdbf656cdedabe239862a866983d92adfb792d4f81d82a3`.

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero. This cycle does not place
blocks.

Run directly with:

```text
java tools/smoke/BasicCraftsCycle.java m297-basic-crafts
```
