# M337 behavior map

One official session crafts the three vanilla utility recipes that M268 and
M316 only used:

- two iron ingots `265` on the 2x2 diagonal (slots `2`+`3`) yield shears `359`
- iron ingot `265` over flint `318` (slots `1`+`4`) yield flint-and-steel `259`
- three iron ingots `265` in workbench slots `1`,`3`,`5` yield empty bucket `325`

Those stacks persist in personal slots `37`, `39`, and `40` across a clean
save plus fresh login. This map does not claim flint-and-steel fire (M268),
shears harvest (M316), or filled-bucket use (M168/M181/M267).

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=personal-2x2+workbench58+ingot265x6+flint318|cause=packet102-window0-shears359+flintsteel259+workbench-bucket325|wire=packet106-accepted+packet200-craft-stat|oracle=result359+result259+result325+fresh-login|column=17,support=4:71:4:1:0,workbench=4:72:4:58:0,shears=359,flintsteel=259,bucket=325,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`24941b7cbf8eca87a6e5f03001a622de0dfb51a8d4e4f754906557bfa7603367`.
