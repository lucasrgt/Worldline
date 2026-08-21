# M304 farmland set

M304 opens the official hoe-till then farmland-trample set. Wooden hoe
Packet15 on dirt `3` produces farmland `60`. The actor then jumps and
falls onto that cell so official `onEntityWalking` converts it back to
dirt `3`. The frozen signal includes both `3->60` and `60->3`. That
exact dirt cell remains after a clean save plus fresh login.

This is the Beta 1.7.3 farmland cycle: till dirt into farmland, then
trample farmland back to dirt. It compounds M307 till and M308 trample
in one official session. It does not claim moisture, wheat, hoe
durability, rain, other hoe materials, or a Worldline soil simulation.

The frozen semantic SHA-256 is
`ce698c2302ea621590b03877774a82c7ea0a5b085bf5536d28093462ed8c121c`.
