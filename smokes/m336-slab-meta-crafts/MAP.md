<!-- worldline-map-schema=1 -->
<!-- boundary=m336-slab-meta-crafts -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=e75d0b2bb489e7ea157ad321c8dc141c57039e5411b10b96658868f3b231cc57 -->

# M336 behavior map

One official workbench epoch crafts the three slab-metadata recipes that
M319 left as stone-only `44:0` and that M234-M236 only placed:

- three sandstone `24` in the top row yield three sandstone slabs `44:1`
- three oak planks `5` in the top row yield three wood slabs `44:2`
- three cobble `4` in the top row yield three cobble slabs `44:3`

Those stacks persist in personal slots `38`, `39`, and `40` across a clean
save plus fresh login. This map does not claim M319 stone slab `44:0`, stair
crafts, or M234-M236 placement facing.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=workbench58+sandstone24x3+planks5x3+cobble4x3|cause=packet102-workbench-crafts|wire=result44:1,44:2,44:3|oracle=craft-output+fresh-login|column=17,support=4:71:4:1:0,workbench=4:72:4:58:0,sandstone=44x3:1,wood=44x3:2,cobble=44x3:3,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`e75d0b2bb489e7ea157ad321c8dc141c57039e5411b10b96658868f3b231cc57`.
