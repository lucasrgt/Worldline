# Semantic Audit — Gameplay and UI

Domain inventory for `origin/main` `1755fa8` (v1.55.0 / M67). Inventory, GUI tree, input
queues, and recipe/item census are already catalogued and consumed.

## Already solid

`CURRENT_SCREEN`, `INVENTORY_SCREEN`, `CONTAINER_CLICK`,
`CONTAINER_SLOTS`, `INVENTORY_KEY`, `ESCAPE_KEY`, item/recipe roles,
`PLAYER_HEALTH`, `HOTBAR_SLOT`. `PACKET14_BLOCK_DIG` is the protocol-14
wire dig packet (`Packet14BlockDig`, official `jv`); M31/M32 send
begin/finish dig and wait for Packet53. That is not
`PlayerController.clickBlock`. `CONTAINER_SLOT_LIST` is `Container.slots`
(`e`); `SLOT_STACK` is `Slot.getStack` (`a`). Those are not
`GuiContainer.inventorySlots` or `IInventory.getStackInSlot`.

## Promoted (M48-M67 wire)

Packet100-106 window classes, Packet15 place, Packet16 hotbar, Packet5
equipment, Packet7/8/38 combat, Packet21/22/29 drop-collect, and
Packet200Statistic. Packet102 is not `CONTAINER_CLICK`. Packet104 is not
`CONTAINER_SLOT_LIST`. Packet15 is not `sendUseItem`. Packet7 is not
`attackEntityFrom`.

## Not promoted

| Candidate | MCP | Official | Descriptor | Why wait |
| --- | --- | --- | --- | --- |
| `GAME_PAUSED` | `Minecraft.isGamePaused` | `o` | `Z` | unused in headless |
| `CLICK_BLOCK` | `PlayerController.clickBlock` | `a` | `(IIII)V` | client controller; Packet14 is the wire |
| `SEND_USE_ITEM` | `PlayerController.sendUseItem` | `a` | `(LEntityPlayer;LWorld;LItemStack;)Z` | client method; Packet15 is the wire |
| `ATTACK_TARGET` | `EntityPlayer.attackTargetEntityWithCurrentItem` | `d` | `(LEntity;)V` | client method; Packet7 is the wire |
| `ATTACK_FROM` | `Entity.attackEntityFrom` | `a` | `(LEntity;I)Z` | health observed via Packet8/38 |
| `PLAY_SOUND` | `SoundManager.playSound` | `b` | `(Ljava/lang/String;FFFFF)V` | audio path not reached |
| `GUI_SET_RESOLUTION` | `GuiScreen.setWorldAndResolution` | `a` | `(LMinecraft;II)V` | not asserted |

Official letter `a` is overloaded on `PlayerController`. Promote by
named method plus descriptor, never by the official alias alone.

## Misfit

`HEADLESS_AUDIO` is attached to `B173Boundaries`, which has no audio
members. `SOUND_MANAGER` is the class; the first-cycle MAP says it is
never initialized.

## Next

Pause and GUI resolution after an explicit headless assertion. Do not
take Aero pause or `currentScreen` writes as catalog evidence.
