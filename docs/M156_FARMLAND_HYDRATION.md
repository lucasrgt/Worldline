# M156 farmland hydration

M156 opens the official hoe-and-soil moisture boundary. Wooden hoe
Packet15 on dirt `3:0` produces farmland `60:0`. Still water `9:0` placed
beside a four-plot raised dirt cluster hydrates at least one cell to
`60:7`. That moist farmland remains after a clean save plus fresh login.

This is the Beta 1.7.3 farmland moisture rule: water in the 9x2x9
neighborhood sets metadata to seven on a random tick. Four adjacent plots
make that sparse tick observable without claiming wheat growth, bonemeal,
trampling, hoe durability, rain, other hoe materials, farmland reverting
under a solid roof, or a Worldline soil simulation.

The fixture installs its still water before tilling, so no dry uncropped
farmland exists during setup and random ticks cannot erase a plot prematurely.
