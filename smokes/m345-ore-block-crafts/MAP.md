<!-- worldline-map-schema=1 -->
<!-- boundary=m345-ore-block-crafts -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=1a72ee9100a460729b226ac6ea350567f9a953cb2d8832d43545f74bdf9f0427 -->

# M345 behavior map

Packet15 places workbench item `58` on a raised stone column. Packet102 then
crafts the ore-block family in that opened 3x3 grid from nine ingredients:

- nine gold ingots `266` yield gold block `41`
- nine iron ingots `265` yield iron block `42`
- nine diamonds `264` yield diamond block `57`
- nine lapis dyes `351:4` yield lapis block `22`

Those four result ids remain in personal storage after a clean save plus
fresh login. This map does not claim M212-M215 place-only of the same
blocks.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=workbench58+gold266x9+iron265x9+diamond264x9+lapis351x9:4|cause=packet102-workbench-ore-block-family|wire=packet106-accepted+packet200-craft-stat|oracle=craft-output-41-42-57-22+fresh-login+not-place|column=17,support=4:71:4:1:0,workbench=4:72:4:58:0,gold=41,iron=42,diamond=57,lapis=22,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`1a72ee9100a460729b226ac6ea350567f9a953cb2d8832d43545f74bdf9f0427`.
