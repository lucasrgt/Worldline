# M559 qualification cycle

`DoubleExtenderSetCycle` rebuilds the cloned sticky-`29` then piston-`33`
double extender in two fresh official server JVMs. Each run sequences the
rear sticky arm, then the front regular arm, so one cobble payload travels
two cells, and reloads the extended chain after save plus fresh login. The
frozen signal includes `cells=2` and `sequenced=29-then-33`. One official
EOF is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`49d44fb82433fdcf9dcf8ca5201aa946b783e9d3539c9689d6a2284af36fac0f`.

Run directly with:

```text
java tools/smoke/DoubleExtenderSetCycle.java m559-double-extender-set
```

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
