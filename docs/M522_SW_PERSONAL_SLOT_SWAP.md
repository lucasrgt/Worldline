# M522-SW personal-slot swap

M522-SW freezes a two-slot personal inventory exchange on the official Beta
1.7.3 server. Stone in window slot 36 and dirt in slot 37 are exchanged by
three accepted left-clicks: take stone, exchange stone with dirt, and place
dirt into the original slot.

The negative probe sends a stale empty prediction for the now-occupied slot.
The server applies the real take, rejects the prediction, and resynchronizes
the window and cursor. A fifth accepted click restores the swapped state. A
clean disconnect/save and same-player relogin must retain dirt in 36 and stone
in 37 with an empty cursor.
