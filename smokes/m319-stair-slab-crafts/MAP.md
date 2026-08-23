<!-- worldline-map-schema=1 -->
<!-- boundary=m319-stair-slab-crafts -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=cec4e38d37d31058c744ff1e9c806d2567fcf878603f2e63cdf7347058f5d553 -->

# M319 behavior map

One official workbench epoch crafts the three vanilla recipes that M186,
M187, and M188 only placed:

- six oak planks `5` in staircase slots `1,4,5,7,8,9` yield four oak stairs `53`
- six cobble `4` in the same staircase yield four cobble stairs `67`
- three stone `1` in the top row yield three stone slabs `44:0`

Those stacks persist in personal slots `38`, `39`, and `40` across a clean
save plus fresh login. This map does not claim placement facing (M186/M187)
or slab metadata variants (M188).

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=workbench58+planks5x6+cobble4x6+stone1x3|cause=packet102-workbench-crafts|wire=result53,67,44|oracle=craft-output+fresh-login|column=17,support=4:71:4:1:0,workbench=4:72:4:58:0,oak=53x4,cobble=67x4,slab=44x3:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`cec4e38d37d31058c744ff1e9c806d2567fcf878603f2e63cdf7347058f5d553`.
