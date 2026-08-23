<!-- worldline-map-schema=1 -->
<!-- boundary=m329-utility-block-crafts -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=b70015b8e4bea597b4b8eeba287d216244d5c1bb9f83a1d7d06120bdb8c5086f -->

# M329 behavior map

One official workbench epoch crafts the three vanilla recipes that M173,
M174, and M189 only placed:

- six sticks `280` in the top two rows yield two fences `85`
- seven sticks `280` in the ladder H yield two ladders `65`
- six oak planks `5` plus three books `340` yield one bookshelf `47`

Those stacks persist in personal slots `38`, `39`, and `40` across a clean
save plus fresh login. This map does not claim fence collision (M173),
ladder facing (M174), or bookshelf placement (M189).

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=workbench58+stick280x6+stick280x7+planks5x6+book340x3|cause=packet102-workbench-crafts|wire=result85,65,47|oracle=craft-output+fresh-login|column=17,support=4:71:4:1:0,workbench=4:72:4:58:0,fence=85x2,ladder=65x2,bookshelf=47x1,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`b70015b8e4bea597b4b8eeba287d216244d5c1bb9f83a1d7d06120bdb8c5086f`.
