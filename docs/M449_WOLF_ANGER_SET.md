# M449 wolf anger set

M449 opens the official compound wild-wolf-anger boundary. Animals are
enabled so a default spawner `52` can be retargeted from `Pig` to `Wolf`.
Packet24 type `95` is struck with diamond sword `276` via Packet7 button 1.
No bone `352` is held. `B173MobTracker.takeTame` stays negative, proving
Packet38 is not tame status `6` or `7`. Packet8 then shows the angry wolf
hitting the actor. The wolf is still living (no Packet38 status `3`).

This is distinct from shipping M420 bone `352` tame plus dye collar
`351:4`. It does not claim breeding, sitting, pack anger, or later wolf
armor. Headless `B173WireClient` only. No GUI. No Aero.

The frozen semantic SHA-256 is
`d3cb895515a26cc4e6b85659bf48537d65e2e91093f07bc035bf0ceffb3d2711`.
