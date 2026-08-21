# M296 qualification cycle

`FurnaceSmeltsCycle` rebuilds three idle furnaces in two fresh official
server JVMs. Each run smelts iron, gold, and pork with coal and compares
the live outputs. One official EOF is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`04ac7e3f754356f848410854d026b17493e75b73dc55473905ec0d45d31787c1`.

Run directly with:

```text
java tools/smoke/FurnaceSmeltsCycle.java m296-furnace-smelts
```
