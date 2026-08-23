# M449 wolf anger set

M449 opens the official compound wild-wolf-anger boundary. Animals are
enabled so a default spawner `52` can be retargeted from `Pig` to `Wolf`.
Packet24 type `95` is struck nonlethally with wood sword `268` via Packet7 button 1.
No bone `352` is held. `B173MobTracker.takeTame` stays negative, proving
Packet38 is not tame status `6` or `7`. Packet8 then shows wolf hostility
hurting the actor. The struck wolf is still living (no Packet38 status `3`).

The former diamond sword `276` killed official wild wolves in one strike and
could only pass through unrelated retaliation. A 24-fence arena now keeps the
selected spawn nearby, and health is sampled before Packet7 so immediate
retaliation is not lost. Packet8 does not identify an attacker, so this contract
does not distinguish the struck wolf from nearby pack hostility.

This is distinct from shipping M420 bone `352` tame plus dye collar
`351:4`. It does not claim breeding, sitting, which wolf delivered the hit, or later wolf
armor. Headless `B173WireClient` only. No GUI. No Aero.

The frozen semantic SHA-256 is
`c459a0789fdb7cd773db7301dbc0e66db10b335a87a017a95d0853817a743057`.
