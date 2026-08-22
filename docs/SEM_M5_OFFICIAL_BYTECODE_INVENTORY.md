# SEM-M5 Official Bytecode Inventory

SEM-M5 reads class-file symbol tables from the pinned official Minecraft Beta
1.7.3 client and dedicated-server JARs without loading or executing any class.
The official JARs remain ignored local inputs and pass `Verify --runtime`
before the audit.

## Exact side-scoped inventory

| Side | Kind | Official class files | Calamus identities | Official-only | Calamus phantom |
| --- | --- | ---: | ---: | ---: | ---: |
| Client | Class | 747 | 678 | 69 | 0 |
| Client | Field | 3,345 | 2,713 | 632 | 0 |
| Client | Method | 5,868 | 2,071 | 3,797 | 0 |
| Server | Class | 444 | 444 | 0 | 0 |
| Server | Field | 1,751 | 1,751 | 0 | 0 |
| Server | Method | 3,417 | 1,352 | 2,065 | 0 |

The complete raw official target is therefore 9,960 client symbol incidences
and 5,612 server symbol incidences. These side-scoped totals must not be added
to the intermediary union as though shared classes were unrelated; later
graph work retains the side identity and links only exact proven shared
records.

## Gap classification

The 69 client classes absent from Calamus are bundled Paulscode Sound, JOrbis,
JOgg, and related codec/OpenAL classes. Their 1,445 members are likewise
official-only-owner records. They are part of the distributed client JAR and
therefore part of Worldline's complete artifact inventory, while remaining
explicitly distinguished from Mojang game classes.

Remaining official-only identities classify as:

| Gap | Client | Server |
| --- | ---: | ---: |
| Official-only classes | 69 | 0 |
| Members of official-only classes | 1,445 | 0 |
| Constructors | 748 | 526 |
| Class initializers | 101 | 69 |
| Other fields in mapped owners | 0 | 0 |
| Other methods in mapped owners | 2,135 | 1,470 |

Calamus has zero phantom class, field, or method identities on both sides.
It covers every field in every mapped game class, but deliberately does not
enumerate all executable methods, especially constructors, class initializers,
unobfuscated overrides, bridges, and helpers.

There are 418 client and 241 server official-only members whose owner/name
matches at least one mapped member while the descriptor differs. These are
`DESCRIPTOR_CONFLICT` candidates, not confirmed mapping errors: legitimate JVM
overloads must first be separated from genuine descriptor disagreement.

## Consequence for the SOTA target

Nostalgia's stated 100 percent beta coverage means complete naming under its
own mapping scope; it does not mean every class-file field and method in the
distributed official JARs has a named entry. Worldline's stronger target now
measures both:

1. complete side-scoped official bytecode inventory;
2. namespace aliases for those identities;
3. separate behaviorally promoted semantic roles.

No official-only symbol receives an invented intermediary or semantic name.
