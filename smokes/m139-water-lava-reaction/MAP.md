# M139 behavior map

The fixture raises a two-cell stone basin away from generated terrain. One cell
contains still lava `11:0`; the adjacent cell is exact air. A fresh client
places still water `9:0`, after which official neighbor processing hardens the
lava source to obsidian `49:0`.

Only the declared water and lava cells enter the causal state hash. The water
source persists as `9:0`, the lava cell persists as `49:0`, and the two exact
deltas are independently observed after save by a third client session.

Frozen semantic SHA-256:
`1ba936e8c311e4af488c393c17f5f68031f6fbb2c7a8b4ae2831985900fcd326`.
