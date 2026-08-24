# M629-DOOR-SOUND-EVENT door sound event

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M629 freezes the server-observable sound event emitted to a nearby peer by direct wooden-door activation in Beta 1.7.3. A closed wooden door begins as lower 64:0 and upper 64:8. Empty-hand activation by the actor opens both cells to 64:4 and 64:12 while a second nearby client receives protocol-14 Packet61 effect 1003 with data 0 at the lower position. The public TestKit contract requires both the state transition and the peer-observed world event. This does not claim local client audio playback, attenuation, close-door randomness, iron doors, trapdoors, redstone activation, or other Packet61 effects.

## Qualification cycle

DataDrivenCycle rebuilds the raised fixed-seed wooden-door fixture in two fresh official dedicated-server JVMs. Each replica pre-seeds the actor and observer at the same position, connects both clients, drains the observer's pre-action world events, activates the closed lower half once, observes Packet61 effect 1003 data 0 at that exact position on the peer, and observes both Packet53 door metadata transitions on the actor. DoorSoundFixture normalizes coordinates while retaining exact state and event semantics as equatable evidence. Headless B173WireClient only. No GUI. No Aero.

Expected signal: `door=64:0/8->4/12,effect=1003:0,event-position=lower,packet=61,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `054db30511dff646e758a311a2c81378fab9e990e24bd4283c4d5b82e0c0320e`.
