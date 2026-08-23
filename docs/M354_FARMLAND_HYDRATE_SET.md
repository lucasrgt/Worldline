# M354 farmland hydrate set

M354 opens the official hoe-till then dry-versus-hydrated farmland set.
Wooden hoe Packet15 on dirt `3` produces farmland `60`. Four raised plots
sit beside still water `9:0` and hydrate to `60:7`. One isolated plot sits
six cells south under a stone rain roof and stays dry `60:0`. Wheat `59`
holds that dry cell against random-tick reversion during the hydration
wait. The frozen signal includes both `dry=60:0` and `hydrated=60:7`.
Those exact states remain after a clean save plus fresh login.

Setup tills and plants the isolated dry control first, then installs water
before tilling the hydrated plots. This removes the unintended dry uncropped
window while preserving the intended dry-versus-hydrated comparison.

This is the Beta 1.7.3 farmland moisture set: water in the 9x2x9
neighborhood writes metadata seven, while a plot outside that neighborhood
with rain blocked stays metadata zero. It compounds hoe-till with dry and
hydrated `60` in one official session. It does not claim M156
hydration-only, M304 till/trample, wheat growth, hoe durability, other hoe
materials, or a Worldline soil simulation.

The frozen semantic SHA-256 is
`31e18ca11dc6928034468d2a503769a4559f5757e60dffc22e8bf85af35522d2`.
