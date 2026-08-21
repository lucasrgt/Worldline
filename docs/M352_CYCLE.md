# M352 qualification cycle

`ToolDurabilitySetCycle` rebuilds the raised stone fixture in two fresh
official server JVMs. Each run Packet14-breaks cobble `4` with wooden
pickaxe `270`, stone `1` with iron pickaxe `257`, and cobble `4` with
gold pickaxe `285`, then reloads the remaining held-stack damage after
save plus fresh login. One official EOF is retried after a 5 second
sleep.

Run directly with:

```text
java tools/smoke/ToolDurabilitySetCycle.java m352-tool-durability-set
```

The frozen semantic SHA-256 is
`46cbf98b50d0745eafee30276fb3d3adafbbd1381f71bf7106012dbe80b75a30`.

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
