# M250 qualification cycle

`RedWoolCycle` rebuilds the raised stone fixture in two fresh official
server JVMs. Each run places red wool item `35` (damage `14`) on the
top face, freezes live `35:14`, and reloads that cell after save plus a
fresh login. The result is distinct from M197 white wool `35:0`.

The frozen semantic SHA-256 is
`c3fcc1daa3851d1bcf11abcdee87a5fa5a626dc413d114ba6ffa58c0692ef726`.

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
