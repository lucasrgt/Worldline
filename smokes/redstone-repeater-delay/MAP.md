# Redstone Repeater Delay Smoke Map

## Claim

The Worldline public runtime can place a standing powered torch, a delay-1
idle repeater facing the torch input, and one dust cell on a deterministic
in-memory b1.7.3 `World`. After placement the dust is unpowered. After six
`MinecraftRuntime.tick()` calls the repeater is the active block, dust
metadata is greater than zero, and `Block.isPoweringTo` is true on the output
face. The same fixture executes against the frozen official obfuscated server
JAR. Two processes per path emit the same canonical trace and signature.

This is a differential smoke of one delay-1 repeater fixture. It does not
claim 2/3/4-tick delay settings, repeater locking, pistons, levers, buttons,
plates, BUD, or quasi-connectivity.

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

| Named symbol | Client | Server | Role in the smoke |
| --- | --- | --- | --- |
| `Block.redstoneRepeaterIdle` | `bi` | `bi` | Delay-1 repeater placed unpowered |
| `Block.redstoneRepeaterActive` | `bj` | `bj` | Expected block after the delay |
| `Block.isPoweringTo` | `c` | `a` | Output-face power after lock-on |
| `World.tick` | `l` | `h` | Advances scheduled repeater updates |
| `IBlockAccess.getBlockMetadata` | `e` | `c` | Observes dust power 0 then >0 |

## Controlled boundaries

- Seed: `17320110707`.
- Torch: `(8, 65, 8)` standing (metadata 5).
- Repeater: `(9, 65, 8)` idle, metadata 1 (input from the torch, delay 1).
- Dust: `(10, 65, 8)`.
- Indirect observer: `(11, 65, 8)`.
- Persistence: in-memory no-op `ISaveHandler` and `IChunkLoader`.

## Nonclaims

Delay settings 2-4, locking from the side, pistons, levers, buttons, pressure
plates, BUD, quasi-connectivity, and any circuit larger than this
torch-repeater-dust line remain outside the claim.

Frozen expected signature SHA-256: 572bf798c9e3922b669928c273e2788199de5af2117737ef11efd7658f67bb7a
