# M323 qualification cycle

`IronToolCraftsCycle` rebuilds the raised workbench fixture in two
fresh official server JVMs. Each run crafts iron sword `267`, pickaxe
`257`, shovel `256`, axe `258`, and hoe `292` from iron ingots `265`
and sticks `280`. One official EOF is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`17587efabb76f538dcae2f11569071d321ff253c298e403c9de331483b463270`.

Run directly with:

```text
java tools/smoke/IronToolCraftsCycle.java m323-iron-tool-crafts
```

Canonical evidence uses two official server JVMs and two client sessions.
