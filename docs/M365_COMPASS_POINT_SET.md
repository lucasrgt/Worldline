# M365 compass point set

M365 opens the official held-compass spawn-point boundary. Compass item
`345` is seeded and Packet16-held; it is not crafted here. That is
distinct from M325, which only workbench-crafts compass `345`, clock
`347`, and empty map `358`.

The official server authors `level.dat` `SpawnX/Y/Z`. The headless
protocol-14 actor looks yaw `0` and yaw `180` while holding compass `345`
and requires the vanilla TextureCompassFX needle to reverse
(`needleDelta=180`). It then Packet13-stands on a second stone cell and
applies the same spawn bearing at both positions. The compass stack
survives a clean save plus fresh login.

World spawn integers are session-authored and are not part of the frozen
hash. The directional oracle is the 180-degree needle reversal plus the
two-cell spawn bearing.

Frozen semantic SHA-256:
`45694ff4da19a7b3c554d405380e9535c075d7fee1f3880674813ed5cc9a8b68`.

This milestone does not claim compass crafting, clock behavior, map use,
held-item GUI, or Nether compass spin. Headless `B173WireClient` only.
No GUI. No Aero.
