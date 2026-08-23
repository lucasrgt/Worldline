<!-- worldline-map-schema=1 -->
<!-- boundary=redstone-wire-power -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=9e732358c31890a2dda2c6fabf5553bcebd81a3808284cfea1bbe85aa4a71645 -->

# Redstone Wire Power Smoke Map

## Claim

The Worldline public runtime can place a standing powered redstone torch and
adjacent dust on a deterministic in-memory b1.7.3 `World`, advance four ticks
through `MinecraftRuntime.tick()`, and observe wire metadata plus
`World.isBlockIndirectlyGettingPowered`. The same fixture executes against the
frozen official obfuscated server JAR. Two processes per path emit the same
canonical trace and signature.

This is a differential smoke of one torch-to-wire fixture. It does not claim
repeater delay, pistons, levers, buttons, plates, BUD, quasi-connectivity, or
whole-graph dust propagation.

## Frozen inputs

| Input | Frozen evidence |
| --- | --- |
| Server JAR | SHA-256 `033a127e4a25a60b038f15369c89305a3d53752242a1cff11ae964954e79ba4d` |
| RetroMCP | Git `9ece383d9bfe993d763d75b503e913f0dfbd8852` |
| Version metadata | SHA-256 `be64dfdab54d85c986140b39345c2abc4fb05ad1dc567041a801e8af64f63944` |
| Tiny mappings | SHA-256 `c170d1fde77fccc36649fb2e09066e04670c835e00c0b46fcf6312c9560453a5` |
| Exceptions | SHA-256 `e02f5e01e0e0de6a4223958ce260ee4b4d5afa830a007a6a861afb55efda039d` |

## Symbol map

The mapping namespace order is `named`, `client`, `server`.
`symbols.map` is the machine-checked inventory of the vanilla surface used by
the smoke and official oracle.

| Named symbol | Client | Server | Role in the smoke |
| --- | --- | --- | --- |
| `World` | `fd` | `dj` | Vanilla state container and tick implementation |
| `World.setBlockAndMetadataWithNotify` | `b` | `b` | Places the standing torch with metadata 5 |
| `World.setBlockWithNotify` | `f` | `e` | Places adjacent redstone dust |
| `World.tick` | `l` | `h` | Vanilla tick root exercised four times |
| `IBlockAccess.getBlockId` | `a` | `a` | Observes torch and wire identities |
| `IBlockAccess.getBlockMetadata` | `e` | `c` | Observes dust power 0-15 |
| `World.isBlockIndirectlyGettingPowered` | `s` | `r` | Observes power at the cell beyond the dust |
| `Block.torchRedstoneActive` | `aR` | `aR` | Powered torch registry field |
| `Block.redstoneWire` | `aw` | `aw` | Dust registry field |
| `Block.canProvidePower` | `f` | `d` | Torch power-provider flag |

## Controlled boundaries

- Seed: `17320110707`.
- Spawn: `(8, 64, 8)`.
- Chunks: a 5x5 preloaded square, bedrock at Y=0 and stone through Y=64.
- Torch: `(8, 65, 8)` standing (metadata 5) on stone.
- Dust: `(9, 65, 8)` adjacent on stone.
- Indirect observer: `(10, 65, 8)` one cell beyond the dust.
- Persistence: in-memory no-op `ISaveHandler` and `IChunkLoader`.
- Input, players, networking, display, audio, and filesystem writes: absent.

## Nonclaims

Repeater delay, unpowered torch burnout, pistons, levers, buttons, pressure
plates, BUD, quasi-connectivity, weak vs strong power, and any circuit larger
than this torch-plus-one-dust fixture remain outside the claim.

Frozen expected signature SHA-256: 9e732358c31890a2dda2c6fabf5553bcebd81a3808284cfea1bbe85aa4a71645
