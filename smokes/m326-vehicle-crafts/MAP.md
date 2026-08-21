# M326 behavior map

Packet15 places workbench item `58` on a raised stone column. Packet102 then
crafts the vehicle item family in that opened 3x3 grid:

- five planks `5` in U-shape slots `4,6,7,9,8` yield boat `333`
- five iron ingots `265` in the same U-shape yield minecart `328`
- chest `54` above minecart `328` yield chest minecart `342`
- furnace `61` above minecart `328` yield furnace minecart `343`

Those four result ids remain in personal storage after a clean save plus
fresh login. This map does not claim M310 boat/minecart rides or M311
storage-cart spawn and window interact.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=workbench58+planks5x5+ingot265x15+chest54+furnace61|cause=packet102-workbench-vehicle-family|wire=packet106-accepted+packet200-craft-stat|oracle=craft-output-333-328-342-343+fresh-login+not-ride+not-spawn|column=17,support=4:71:4:1:0,workbench=4:72:4:58:0,crafts=333,328,342,343,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`1109c4ce19cf7f23d5156d80cef725329fc62a68c438e24d4294aa468e088bdc`.
