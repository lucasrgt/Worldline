# M279 button press

M279 freezes the official stone-button press pulse. It clones the M165
east-face fixture: item 77 is placed with Packet15 onto a raised stone
column as block `77:1`. That place is only setup. The milestone oracle is
empty-hand Packet15, which sets the power bit to `77:9`.

Vanilla `BlockButton.tickRate()` is 20 ticks in b1.7.3. After a 200-tick
water-column settle, `sustainTicks` verifies that the button stays powered
for one tick and returns exactly to `77:1` after the delay.

A clean save plus fresh Packet51 login retains the unpowered `77:1` button.
Wooden buttons do not exist in Beta 1.7.3. This milestone does not claim
redstone wire consumers, attached-block neighbor updates, or arrow press.
