# M611 behavior map

The fixture raises a two-block stone pad and places one chest `54:0`. Empty-hand
Packet15 opens Packet100 title `Chest` with 27 owned slots and a 63-slot
Packet104 view. Packet101 closes that window. A second chest `54:0` is then
placed on the east neighbor. Opening either cell now yields Packet100 title
`Large chest` with 54 owned slots and a 90-slot Packet104 view.

Both chest cells and the merged 54-slot window survive a clean save plus fresh
login. This map is distinct from M232 single-chest place and orientation, which
never opens Packet100, and from a remaining 27-slot `Chest` window.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+two-adjacent-chest54|cause=packet15-item54+packet100-owned27-title-Chest+packet15-item54+packet100-readUTF-owned54-title-Large-chest|wire=packet53-chest54:0+packet53-chest54:0+packet100-single27-then-merged54|oracle=double-chest-merge-not-single-place-orient|column=17,support=4:71:4:1:0,east=5:71:4:1:0,left=4:72:4:54:0,right=5:72:4:54:0,single=title=Chest,owned=27,total=63,merged=title=Large chest,owned=54,total=90,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`638adc227adc9c23b9840e4745a2596cd5dde899442a3510271da150d03980c9`.
