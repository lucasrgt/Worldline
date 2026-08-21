# M361 behavior map

Packet15 places two east-facing ladders `65:5` on a raised two-stone face.
The same east column is first Packet13-jumped as air, then Packet13-climbed
on live ladder `65` for ten ticks. The frozen signal includes ladder `65`
plus the climbed pose delta (`climb=2000` milli-blocks, two cells), and
that delta exceeds the air column (`air=495`). Both ladder cells survive a
clean save plus fresh login. This is not M174 place-only facing metadata.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+ladder65-east-column2|cause=packet15-item65-place+packet13-climb|wire=packet53-ladder65:5+packet13-pose-delta|oracle=ladder-climb-two-cells-vs-air-column+fresh-login|column=18,support=4:71:4:1:0,upper=4:72:4:1:0,ladder=5:71:4:65:5+5:72:4:65:5,face=east,ticks=10,air=495,climb=2000,climbed=true,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`113dccdda9b6bd0140c7aea5b255db993bb9063c6d64ef9370f1fb9925c26340`.
