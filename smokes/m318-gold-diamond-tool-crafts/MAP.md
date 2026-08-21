# M318 behavior map

A raised-stone workbench `58` accepts Packet102 matrix placement of gold
ingots `266`, diamonds `264`, and sticks `280`. Official result takes freeze
gold tools `283,284,285,286` and diamond tools `276,277,278,279`.

This map does not claim tool use, durability, or personal 2x2 crafting.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=workbench58+gold266x9+diamond264x9+stick280x14|cause=packet102-workbench-matrix+result-take|wire=packet106-accepted+packet200-craft-stat|oracle=gold-family-283-284-285-286+diamond-family-276-277-278-279|gold=283+285+286+284,diamond=276+278+279+277,column=17,support=4:71:4:1:0,workbench=4:72:4:58:0,actions=77,persisted=true,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`ea2a3772ad997141d967212b9f93a52ec0b5f633dde29b2b0192e844a377005e`.
