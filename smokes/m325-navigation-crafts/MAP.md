<!-- worldline-map-schema=1 -->
<!-- boundary=m325-navigation-crafts -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=904591e822865303c647ea818403edb8d115b37da19262cb96387da6f2e4302d -->

# M325 behavior map

One official workbench epoch crafts the three vanilla navigation recipes:

- four iron ingots `265` around redstone `331` yield compass `345`
- four gold ingots `266` around redstone `331` yield clock `347`
- eight paper `339` around a crafted compass yield empty map `358`

Those stacks persist in personal slots `37`, `39`, and `41` across a clean
save plus fresh login. A second compass is crafted only to feed the map
recipe; the kept compass is the first result. This map does not claim map
use, map filling, or compass/clock GUI.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=workbench58+iron265x8+gold266x4+redstone331x3+paper339x8|cause=packet102-workbench-crafts|wire=result345,347,358|oracle=craft-output+fresh-login|column=17,support=4:71:4:1:0,workbench=4:72:4:58:0,compass=345,clock=347,map=358,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`904591e822865303c647ea818403edb8d115b37da19262cb96387da6f2e4302d`.
