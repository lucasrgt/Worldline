# M165 stone button pulse

M165 opens the official stone-button pulse. Item 77 is placed with Packet15
onto the east face of a raised stone column as block `77:1`. Empty-hand
Packet15 then sets the power bit to `77:9`. Vanilla `BlockButton.tickRate()`
is 20 ticks in b1.7.3; the smoke verifies that delay with `sustainTicks`
after a 200-tick water-column settle.

A clean save plus fresh Packet51 login retains the unpowered `77:1` button.
Wooden buttons do not exist in Beta 1.7.3. This milestone does not claim
redstone wire consumers, attached-block neighbor updates, or arrow press.
