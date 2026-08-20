# M303 crop harvests

M303 is the compound harvest-drop SET for wheat `59`, sugar cane `83`, and
cactus `81`. It clones the M179 hoe-and-seed plant, the M304 bone-meal mature
write, the M159/M305 reed place-and-break, and the M167/M306 cactus
place-and-break. One headless `B173WireClient` session plants all three
families on one raised column and Packet14-harvests each cell.

Mature wheat `59:7` emits Packet21 wheat `296:1:0`. Planted seeds remain
item `295`. Reed `83` emits Packet21 `338:1:0`. Cactus `81` emits Packet21
`81:1:0`. Sand and cane dirt remain. Health stays 20.

This SET does not wait for random-tick wheat growth, cane height `>= 2`, or
cactus age. It does not claim seed-count lottery Packet21 `295`, item
collection, trampling, or sibling individual milestones M304/M305/M306.

Frozen semantic SHA-256:
`33bca9f328ddb3c028b792f70233157d997e260e28d47d0115069be6bcba67f0`.
