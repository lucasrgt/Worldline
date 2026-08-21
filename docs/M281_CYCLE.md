# M281 qualification cycle

`LightBlueWoolCycle` rebuilds the raised stone fixture in two fresh
official server JVMs. Each run places light-blue wool item `35` (damage
`3`) on the top face, freezes live `35:3`, and reloads that cell after
save plus a fresh login. The result is distinct from other wool metas.

The frozen semantic SHA-256 is
`49e519da435b759ce7053b4105c826cbae35a31badd8bc4f4e50d0cd48617e1f`.

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
