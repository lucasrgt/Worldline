# M347 behavior map

A raised-stone workbench `58` accepts Packet102 matrix placement of gold
ingots `266`, diamonds `264`, and sticks `280`. Official result takes freeze
gold hoe `294` and diamond hoe `293`.

This map does not claim tool use, durability, farmland tilling, or the M318
sword/pick/axe/shovel families. It is distinct from M323 iron hoe `292`.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=workbench58+gold266x2+diamond264x2+stick280x4|cause=packet102-workbench-matrix+result-take|wire=packet106-accepted+packet200-craft-stat|oracle=gold-hoe-294+diamond-hoe-293|gold=294,diamond=293,column=17,support=4:71:4:1:0,workbench=4:72:4:58:0,actions=17,persisted=true,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`db8eed6611d61cb0063b32dcf170a0fd66d46799102196c45721842544e6515b`.
