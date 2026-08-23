<!-- worldline-map-schema=1 -->
<!-- boundary=redstone-catalog -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=aggregate:redstone-runtime-oracles -->

# Redstone semantics map

Closed `redstone` catalog slice. Official client/server aliases come from the
mapped b1.7.3 workspace. Executable oracles live in `redstone-wire-power`,
`redstone-repeater-delay`, `redstone-repeater-delays`, `redstone-lever-button`,
and `redstone-piston-extend`.

| Role | Mapped symbol | Client | Server |
| --- | --- | --- | --- |
| `REDSTONE_WIRE_TYPE` | `BlockRedstoneWire` | `sm` | `lo` |
| `REDSTONE_WIRE` | `Block.redstoneWire` | `aw` | `aw` |
| `REDSTONE_TORCH_TYPE` | `BlockRedstoneTorch` | `db` | `by` |
| `REDSTONE_TORCH` | `Block.torchRedstoneActive` | `aR` | `aR` |
| `REDSTONE_REPEATER_TYPE` | `BlockRedstoneRepeater` | `wo` | `oi` |
| `REDSTONE_REPEATER_IDLE` | `Block.redstoneRepeaterIdle` | `bi` | `bi` |
| `REDSTONE_REPEATER_ACTIVE` | `Block.redstoneRepeaterActive` | `bj` | `bj` |
| `REDSTONE_LEVER_TYPE` | `BlockLever` | `xr` | `pd` |
| `REDSTONE_LEVER` | `Block.lever` | `aK` | `aK` |
| `REDSTONE_BUTTON_TYPE` | `BlockButton` | `oi` | `iz` |
| `REDSTONE_BUTTON` | `Block.button` | `aS` | `aS` |
| `REDSTONE_SCHEDULE` | `World.scheduleUpdateTick` | | `c` |
| `REDSTONE_PISTON_TYPE` | `BlockPistonBase` | `jq` | `gi` |
| `REDSTONE_PISTON` | `Block.pistonBase` | `aa` | `aa` |
| `REDSTONE_PISTON_HEAD` | `Block.pistonExtension` | `ab` | `ab` |
| `REDSTONE_PISTON_HEAD_TYPE` | `BlockPistonExtension` | `h` | `g` |
| `REDSTONE_PISTON_MOVING_TYPE` | `BlockPistonMoving` | `ut` | `mz` |
| `REDSTONE_PISTON_MOVING` | `Block.pistonMoving` | `ad` | `ad` |
| `BLOCK_PROVIDES_POWER` | `Block.canProvidePower` | `f` | `d` |
| `BLOCK_POWERING_TO` | `Block.isPoweringTo` | `c` | `a` |
| `WORLD_INDIRECT_POWER` | `World.isBlockIndirectlyGettingPowered` | `s` | `r` |

Nonclaims: repeater locking is absent in b1.7.3; pressure plates need
`updateEntities` plus non-deterministic `EntityItem` motion; BUD is an
update-order quirk, not a named block. Those stay unmapped.
