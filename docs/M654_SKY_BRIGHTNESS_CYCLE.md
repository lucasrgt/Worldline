# M654-SKY-BRIGHTNESS-CYCLE sky brightness cycle

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M654 freezes the exact clear-weather Overworld skylight-subtraction cycle at canonical Beta 1.7.3 world times. The public evidence closes from subtraction 0 through the dusk ramp to the night plateau 11 and back through the dawn ramp to 0. It does not claim renderer color, weather attenuation, or Nether lighting.

## Qualification cycle

Run executes two fresh mapped server worlds and two direct official-server oracle worlds. Each in-memory world rejects terrain access, sets thirteen canonical world times, recomputes the clear-sky field, and records both time and skylight subtraction as equatable evidence. Qualification requires deterministic replicas and a byte-identical mapped-versus-official canonical trace.

Expected signal: `weather=clear,times=0:6000:12000:12500:13000:13500:14000:18000:22000:22500:23000:23500:23999,skylight-subtracted=0:0:0:3:6:9:11:11:11:9:6:3:0,replicas=4,oracle=mapped-official-match`.

Frozen semantic SHA-256: `a9f7037ea337b6a85f2564061420b3295ef1b8b3cd1c07978f460663cd037fb0`.
