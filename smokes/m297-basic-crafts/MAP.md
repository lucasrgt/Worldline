# M297 basic crafts map

## Stable boundary

- `B173PlayerSeed.writeInventory` seeds oak log item `17` and coal item `263`
  into hotbar slots that become window-0 slots 36 and 37.
- Seventeen accepted Packet102 transactions in personal window 0:
  - left-click the log into matrix slot 1 and take planks `5x4:0`
  - M220-style right-place two planks into a vertical 2x2 pair (slots 1+3)
    and take sticks `280x4:0`
  - left-click coal into slot 1, right-place one crafted stick into slot 3,
    and take torches `50x4:0`
- Taking result slot 0 with each exact prediction is the Packet106 recipe
  oracle. Remaining planks `5x2`, torches `50x4`, and sticks `280x3` survive
  a clean save plus fresh login.

## Oracle

The official Minecraft Beta 1.7.3 dedicated-server JAR owns SlotCrafting
acceptance, Packet106 acknowledgements, ingredient consumption, player NBT
persistence, and login Packet104 bootstrap. Protocol 14 does not transmit
SlotCrafting result updates on accepted grid clicks, so each result stack is
the ACK-correlated local model confirmed by the slot-0 take.

This is player-inventory 2x2 crafting. It does not open a 3x3 workbench or
send Packet15.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=personal-2x2-log17+coal263|window0=log-to-planks5+vertical-1+3-sticks280+coal1-stick3-torch50|cause=packet102-window0-left+button1-right-place|wire=packet106-accepted|oracle=result5x4+result280x4+result50x4+fresh-login|result=5x4:0+280x4:0+50x4:0,taken=true,stored=36:5x2:0+37:50x4:0+38:280x3:0,actions=17,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`f62ec64a6ea2c9990cdbf656cdedabe239862a866983d92adfb792d4f81d82a3`.

## Non-claims

M297 does not claim other 2x2 recipes, 3x3 workbench recipes, metadata
variants, shift clicks, M210 plank placement, M175 torch placement, or
generic inventory automation.
