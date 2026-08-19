# BetaVault Logical Item Reference Extension

Worldline now has a neutral logical item-reference boundary intended for mods
whose object identity cannot safely fit inside Minecraft Beta 1.7.3's 16-bit
damage field.

The production module contains no Minecraft, BetaVault, or BetaEnergistics
dependency. It defines the canonical value, the opt-in carrier contract, and a
strict nullable wire codec. The runtime implementation remains a controlled
StationAPI extension maintained by the consuming integration.

The canonical form is:

```text
minecraft:<32-lowercase-hex-world>|<32-lowercase-hex-object>|<namespace.schema/version>
```

The first qualified consumer uses the real BetaEnergistics cell facade and the
real BetaVault save-bound store. A physical ItemStack carries only the stable
reference; cell contents remain in BetaVault. Copying the physical stack copies
the reference and therefore aliases one logical cell, while optimistic store
transactions remain the authority against duplication.

This extension is deliberately separate from numbered Worldline milestones so
it can be integrated onto the latest official line without colliding with
unpublished milestone work. See the smoke MAP for exact claims and nonclaims.
