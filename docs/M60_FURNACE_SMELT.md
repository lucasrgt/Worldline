# M60 Furnace Smelt

M60 adds one bounded furnace workflow: open a single furnace, move exactly one
sand item and one coal item from personal storage into its empty input and fuel
slots, observe the complete live smelt, and close the window through M58.

Packet100 type 2 is decoded as the exact descriptor `Furnace` with three owned
slots. Its matching Packet104 contains 39 slots: input 0, fuel 1, output 2, then
the 36 personal storage slots. The uniform mapping is
`combinedSlot = personalSlot - 6`.

Four Packet102 actions load the furnace. Actions 1 and 3 take sand and coal from
the combined player tail; actions 2 and 4 place them in owned slots 0 and 1.
Every prediction is adapter-owned and every state transition is committed only
after a correlated Packet106 true. The active 39-slot view, canonical personal
view, and cursor are validated and published together.

Packet103 then reconciles asynchronous input, fuel, and output changes. Packet105
is correlated to the active window-open epoch and records cook time (property 0),
remaining burn time (1), and current fuel duration (2). The exact official live
oracle reaches cook 199, burn and fuel duration 1600, then reports cook reset 0
and burn 1401 after producing one glass item in output slot 2.

## Boundaries

M60 claims only sand `12x1:0` plus coal `263x1:0` producing glass `20x1:0` in a
fresh empty single furnace. It does not claim arbitrary recipes or fuels, output
pickup, experience, shift/right clicks, merges, rejected container recovery,
concurrent mutation, or restart-stable progress telemetry.
