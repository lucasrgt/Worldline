# M299 behavior map

Packet15 places workbench `58` on a raised stone column. Packet102 then
crafts the stone-tool family from cobble `4` and sticks `280`. Official
result IDs are sword `272`, shovel `273`, pickaxe `274`, axe `275`, and
hoe `291`, never wood tools `268`-`271` or `290`. Those stacks survive a
clean save plus fresh login.

This map does not claim wood-tool crafts, iron tools, or durability.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=workbench58+cobble4+stick280|cause=packet102-craft-stone-tools|wire=packet104-results-272,273,274,275,291|oracle=workbench-family+fresh-login|results=272,273,274,275,291,left=4:53+280:55,column=17,workbench=4:72:4:58:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`c7503bc481ed407a57f6a750986b748f269a4222a4a8a2b9a3e26c5a12557c54`.
