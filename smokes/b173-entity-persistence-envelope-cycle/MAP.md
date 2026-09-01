<!-- worldline-map-schema=1 -->
<!-- boundary=b173-entity-persistence-envelope-cycle -->
<!-- nonclaims=world-save-container,chunk-membership,rider-graph,owner-entity-reference,rendering -->
<!-- frozen-trace=6b561fdb5e98d949487ca7629a0ad93a794bb8f214edd386252ffec065797994 -->

# Beta 1.7.3 entity persistence-envelope subsystem

The public `EntityPersistenceFixture` records native NBT serialization and `EntityList`
reconstruction for every concrete registered entity. It preserves the exact registry name and
runtime class, common position, motion, rotation, fall, fire, air, and ground state, plus the full
entity-specific NBT payload. A second native serialization must be byte-identical.

Registered type `48` names the abstract `EntityLiving` base class. It cannot be constructed or
persisted as a concrete native entity, so its save-reload cell is explicitly non-applicable. This
map does not claim world/chunk container persistence, rider graphs, live owner references, or
rendering. Atlas imports this bounded result as
`atlas.experiment.b173-entity-persistence-envelope-cycle` from the frozen smoke descriptor.

Frozen aggregate signal:
`family=entity-persistence-envelope,subjects=23,claims=23,layers=UNIVERSALx23,abstract=entity/048:NOT_APPLICABLE,reconstructed=23,type-exact=23,nbt-exact=23,deterministic=true,evidence=223613ec6363b778024893808b4b52e023150f74b2e702d92abaafaffe4ddb9f`.

Qualified semantic signature:
`6b561fdb5e98d949487ca7629a0ad93a794bb8f214edd386252ffec065797994`.
