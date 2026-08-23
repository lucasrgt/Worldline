# M179 wheat

M179 opens the official wheat-planting boundary. Wooden hoe Packet15 on dirt
`3:0` produces farmland `60:0`. Still water `9:0` placed beside a four-plot
raised dirt cluster hydrates at least one cell to `60:7`. Seeds item `295`
Packet15 on that moist plot plants wheat block `59:0`. That newly planted
crop remains `59:0` after a short live hold and after a clean save plus
fresh login.

This is the Beta 1.7.3 crop-placement rule: `ItemSeeds` accepts the top face
of farmland and writes `BlockCrops` above it at metadata zero. Four adjacent
plots reuse the M156 moisture fixture so the sparse hydration tick is
observable before planting.

The fixture installs water before tilling the four crop plots, eliminating the
brief dry uncropped state in which a vanilla random tick could restore dirt.

The frozen semantic SHA-256 is
`00d861629497b91621c26cc02b6ec8d56763ad9b4f365028fd10188e36694be8`.

The milestone does not wait for random-tick growth to `59:7`. It does not
claim harvest, bone meal (wheat has no bone-meal path in b1.7.3 the same
way later editions do), trampling, hoe durability, rain, other hoe
materials, or a Worldline crop simulation.
