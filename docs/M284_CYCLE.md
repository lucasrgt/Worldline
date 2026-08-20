# M284 qualification cycle

`GrayWoolCycle` rebuilds the raised stone fixture in two fresh official
server JVMs. Each run places gray wool item `35` (damage `7`) on the
top face, freezes live `35:7`, and reloads that cell after save plus a
fresh login. The result is distinct from light-gray `35:8` and black
`35:15`.

The frozen semantic SHA-256 is
`73e9c154cc10de9ba90cb2af73ce28ad87ed76e593fc4961f12616d08161821c`.

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
