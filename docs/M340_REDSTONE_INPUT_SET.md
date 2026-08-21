# M340 redstone input set

M340 freezes the official lever-and-stone-button input family together.
Packet15 places lever `69` on the raised stone column's east face and
stone button `77` on an east face of a south pad. Empty-hand Packet15
latches the lever on and off (`69:1 -> 69:9 -> 69:1`), then pulses the
button (`77:1 -> 77:9 -> 77:1`) through the vanilla 20-tick delay. A
clean save plus fresh login keeps both unpowered.

This milestone is a set of two redstone inputs, not a single-cell map.
It is distinct from M115 lever-only activation, M165/M279 button-only
pulses, and M295 pressure plates. It does not claim wire consumers,
wooden buttons, attached-block neighbor updates, or arrow press.
Headless `B173WireClient` only. No GUI. No Aero.

The frozen semantic SHA-256 is
`366f2922f527ce87c4818902a75b3e646c8a6e5946e6b84838fe3c9918f0c456`.
