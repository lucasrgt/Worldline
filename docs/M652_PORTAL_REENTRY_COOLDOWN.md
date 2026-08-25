# M652-PORTAL-REENTRY-COOLDOWN portal re-entry cooldown

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

Official Beta 1.7.3 suppresses an immediate return while a player remains in the generated arrival portal. After an Overworld-to-Nether transition, another full 120-tick portal-contact observation leaves the same player inside the collision region in dimension minus one. A server-corrected trajectory then proves that player left the portal for a bounded 220-tick window and re-entered it before a 120-tick residence produced Packet9 back to dimension zero. Both generated sides retain six portal blocks within fourteen obsidian blocks, and the returned dimension persists. This contract does not claim the minimum release duration, dynamic portal coordinates, coordinate scaling, non-player entities, or portal construction limits.

## Qualification cycle

DataDrivenCycle rebuilds two fresh official dedicated-server replicas. A protocol-14 player constructs and activates the already-qualified upright fourteen-obsidian source portal, remains in it for the outbound residence window, and discovers the generated Nether portal by its six-cell geometry. The blocked treatment confirms the same player's corrected pose remains inside the destination collision region after 120 ticks. The control records that player outside the collision region at the end of exactly 220 observed ticks, then records the same player inside again before the return residence. Packet9 dimensions, the inside-outside-inside path, portal geometry, and saved player dimension flow through PortalReentryCooldownFixture as equatable evidence. Headless only. No GUI. No Aero.

Expected signal: `dimensions=0->-1->-1->0,column=10,source=4:65:4,sourcePortal=6:14,destinationPortal=6:14,contactHold=120,outsideRelease=220,path=inside->outside->inside,sameActor=true,returnResidence=120,returnPortal=6:14,persisted=0,clients=1,disconnect=clean`.

Frozen semantic SHA-256: `7d049dc9670176f8a7d18b746d9deba02609d0523483c83589572a163bc4511c`.
