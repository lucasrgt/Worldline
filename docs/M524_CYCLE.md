# M524-SW-DIG-STATUS-BOUNDARIES Sw dig status boundaries

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

This milestone freezes the behavior identified by its expected signal and semantic signature.

## Qualification cycle

Status: **GO** after two fresh official-server executions agreed and the trace
was frozen.

The cycle compiles the server adapter plus smoke-only raw Packet14 accessor,
runs two independent server workspaces, and requires identical signal, trace,
and signature. Official server execution remains serialized by the external
runtime lock.

Frozen semantic SHA-256:
`bb181bdd9db372111cec4f232aad960bc58be6fc343dc384dc4ccb89dd1f32ea`.

Expected signal: `column=17,support=4:71:4,positive=4:72:4:3:0->0:0,status=0+2,ignored=3:72:4:3:0->3:0,status=1,orphan=5:72:4:3:0->3:0,status=2-only,tool=284,persisted=true,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `bb181bdd9db372111cec4f232aad960bc58be6fc343dc384dc4ccb89dd1f32ea`.
