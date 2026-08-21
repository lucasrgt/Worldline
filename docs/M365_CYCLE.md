# M365 qualification cycle

`CompassPointSetCycle` rebuilds the raised-stone compass fixture in two
fresh official server JVMs. Each run Packet16-holds compass `345`, reads
official spawn from `level.dat`, Packet12-looks yaw `0` and yaw `180`,
and Packet13-stands on a second cell. The frozen signal includes compass
`345` plus the `needleDelta=180` directional oracle. One official EOF is
retried after a 5 second sleep.

Run directly with:

```text
java tools/smoke/CompassPointSetCycle.java m365-compass-point-set
```

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.

The frozen semantic SHA-256 is
`45694ff4da19a7b3c554d405380e9535c075d7fee1f3880674813ed5cc9a8b68`.
