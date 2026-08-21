# M288 qualification cycle

`BrownWoolCycle` rebuilds the raised stone fixture in two fresh official
server JVMs. Each run places brown wool item `35` (damage `12`) on the
top face, freezes live `35:12`, and reloads that cell after save plus a
fresh login. The result is distinct from other wool metas. One official
EOF is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`98dd28d183167f3553fde2b22ed4f84c648da9eb7ba620ff9ec066c95de722a8`.

Run directly with:

```text
java tools/smoke/BrownWoolCycle.java m288-brown-wool
```

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
