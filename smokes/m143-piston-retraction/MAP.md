# M143 behavior map

The smoke constructs the exact west-facing normal-piston fixture from M142 and
first activates it. After save and fresh login, the independent treatment
client requires exact extended lever/base/head/displaced-stone states.

Deactivation produces raised transitions `69:9→69:1`, `33:12→33:4`, and
`34:4→0:0`. The pushed cell remains stone `1:0`. The digest spans all cells
from the raised support through Y 127 and contains exactly those three deltas.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=fresh-extended-normal-piston33+head34+displaced-stone1|settle=200+10+save+fresh+10ticks|cause=packet15-lever-deactivate|effect=official-piston-retract+head-removal+stone-retained|observation=fresh-login-packet51|column=10,lever=5:64:4:9->1,piston=4:65:4:12->4,head=34:4->0:0,pushed=1:0->1:0,raised-states=3:f46a69c3eadf32fc3b03719c4910d76dc948736b0bf0483b3d3bac6a16e98017|disconnect=clean
```

Frozen semantic SHA-256:
`ed36c9824aa5c765b651fa5a53fa268e5427568f47fabaeb082ec26f7639e2e1`.
