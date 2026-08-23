# M506-SW-SHEEP-SHEARED-PERSISTENCE Sw sheep sheared persistence

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

This milestone freezes the behavior identified by its expected signal and semantic signature.

## Qualification cycle

Status: **GO** after two independent three-restart official-server cycles
agreed and the semantic trace was frozen.

The cycle compiles the server adapter plus smoke-only sheep metadata and NBT
accessors. Every official process is owned by the central coordinator and is
serialized through the external runtime lock.

Frozen semantic SHA-256:
`57aca1de84ec46a162610d18a48ba190b6128e62cc628d70e9e6ef92361790bd`.

Expected signal: `column=17,platform=5x5-grass,spawners=4:72:4+5:72:4,mobs=type91+type91,red=14->30,persisted=30,control=0,repeat=no-new-wool,nbt.Sheared=1->0,mutated=14->30,changed=1,clients=3,restarts=3`.

Frozen semantic SHA-256: `57aca1de84ec46a162610d18a48ba190b6128e62cc628d70e9e6ef92361790bd`.
