<!-- worldline-map-schema=1 -->
<!-- boundary=container-transaction -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=4d18743104fc8bb5efa84e46268323c5d77af8d121e315b156ea3305cf69b5de -->

# M60 Furnace Smelt Map

| Boundary | Exact evidence |
| --- | --- |
| Open | Packet100 type 2 / `Furnace` / 3 is paired with Packet104 size 39 |
| Mapping | Personal slots 9-44 map to combined furnace slots 3-38 with `combined=personal-6` |
| Load | Packet102/106 actions 1-4 move sand36 to input0 and coal37 to fuel1 |
| Atomic state | Active view, canonical window 0, and cursor commit together after each ACK |
| Slots | Packet103 empties input/fuel and publishes glass20x1 in output2 |
| Progress | Packet105 proves cook199, burn1600, total1600, reset0, completion burn1401 |
| World | Both remote caches observe the lit furnace block ID 62 |
| Close | M58 accepted window-0 proof closes the exact completed furnace window |

M60 does not claim generic smelting, alternate fuel, output collection, XP,
container rejection recovery, merges, shift/right clicks, or progress persistence.

Frozen expected signature SHA-256: `4d18743104fc8bb5efa84e46268323c5d77af8d121e315b156ea3305cf69b5de`
