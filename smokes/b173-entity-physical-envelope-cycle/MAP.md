<!-- worldline-map-schema=1 -->
<!-- boundary=b173-entity-physical-envelope-cycle -->
<!-- nonclaims=abstract-entity-materialization,movement-response,environment-collision,entity-pair-dynamics,selection-rendering -->
<!-- frozen-trace=f0eb393236dd24a42a16a2bdfb5a7cd44b5c8304e40599fe894f8d7fe71fb7dd -->

# Beta 1.7.3 entity physical-envelope subsystem

The public `EntityPhysicalEnvelopeFixture` records each concrete registered entity's canonical
dimensions, vertical offset, derived axis-aligned bounding box, collision eligibility, pushability,
and pair-collision-box disposition. The mapped official client is executed twice and the complete
evidence document must be byte-identical.

Registered type `48` names the abstract `EntityLiving` base class. It cannot be constructed as a
native entity, so its collision-shape cell is explicitly non-applicable. The map does not substitute
a subclass and does not claim movement response, environment collision outcomes, pairwise dynamics,
or rendering.

Frozen aggregate signal:
`family=entity-physical-envelope,subjects=23,claims=23,layers=ARCHETYPEx13+SINGULARx10,abstract=entity/048:NOT_APPLICABLE,deterministic=true,evidence=5e84b352af9c02a638cd75f5b10c849d806c09694871cbacd9485d0d38c2c728`.

Qualified semantic signature:
`f0eb393236dd24a42a16a2bdfb5a7cd44b5c8304e40599fe894f8d7fe71fb7dd`.
