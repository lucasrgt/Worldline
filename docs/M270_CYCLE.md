# M270 qualification cycle

`IronHelmetCycle` equips iron helmet `306` in two fresh official server
JVMs. Each run left-clicks personal window 0 slot `36` into armor slot `5`,
correlates peer Packet5 slot `4` as item `306`, and reloads that state after
save plus fresh login. One official EOF is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`d62f78b5a3cb690f1845fa802de6bfa0cca27bc60ed090c60f93fdc665bf4f07`.

Canonical evidence uses two official server JVMs and eight client sessions.

Run directly with:

```text
java tools/smoke/IronHelmetCycle.java m270-iron-helmet
```
