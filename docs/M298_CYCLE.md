# M298 qualification cycle

`WoodToolCraftsCycle` rebuilds the raised stone fixture in two fresh
official server JVMs. Each run places workbench `58`, opens the 3x3
grid, crafts wooden sword `268`, pickaxe `270`, axe `271`, shovel `269`,
and hoe `290` from oak planks `5` and sticks `280`, takes those results,
and reloads them after save plus a fresh login. One official EOF is
retried after a 5 second sleep.

Frozen semantic SHA-256:
`2b099c580ef169af939546718df1c4ae560e5f875f92960733fbcc026a3982bf`.

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.

Run directly with:

```text
java tools/smoke/WoodToolCraftsCycle.java m298-wood-tool-crafts
```
