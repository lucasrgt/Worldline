<!-- worldline-map-schema=1 -->
<!-- boundary=m371-machine-block-crafts -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=51d13daf2febf456a423e84d136707a77b9117668bc3979f4b52514bdbb26c7e -->

# M371 behavior map

Packet15 places workbench item `58` on a raised stone column. Packet102 then
crafts the related machine-block family in that opened 3x3 grid:

- five gunpowder `289` in checkerboard slots `1,3,5,7,9` plus four sand `12`
  in slots `2,4,6,8` yield TNT `46`
- three oak planks `5`, four cobble `4`, one iron ingot `265`, and one
  redstone `331` yield piston `33`
- slime ball `341` above a crafted piston yields sticky piston `29`

A second piston is crafted so both `33` and `29` remain. Those three result
ids persist in personal storage after a clean save plus fresh login. This map
does not claim M219 TNT place, M137 TNT explosion, M294/M293 piston place,
M142-M147 piston motion, or M333 dispenser place/eject.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=workbench58+sand12x4+gunpowder289x5+planks5x6+cobble4x8+ingot265x2+redstone331x2+slime341|cause=packet102-workbench-tnt46+piston33+sticky29|wire=packet106-accepted+packet200-craft-stat|oracle=craft-output-46-33-29+fresh-login+not-place+not-eject|column=17,support=4:71:4:1:0,workbench=4:72:4:58:0,crafts=46,33,29,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`51d13daf2febf456a423e84d136707a77b9117668bc3979f4b52514bdbb26c7e`.
