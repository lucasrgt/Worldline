# Worldline Mapping Constitution

Status: adopted by MAP-01 for the Beta 1.7.3 line.

## Complete-game boundary

Worldline defines complete-game mapping coverage as the complete maintained
symbol graph for which every identity has at least two independent mapping
sources. SEM-M13 satisfies that boundary with 6,475 qualified identities out
of 6,486 graph records. Membership and report order are derived only from the
canonical graph, never from milestone source, descriptors, or documentation.

The exact runtime-reconstructed policy is
`mappings/b1.7.3/sem-m13.properties`; its report SHA-256 is
`fe976b1eff57d67cdb65a34e5efd1ee781a6e39cd988b3c6db7d5a33ac8ae9cf`.
Changing a source artifact, graph identity, qualification source, exclusion,
or report row fails closed.

## Formal retractions

Eleven legacy Nostalgia-only records are outside the maintained graph. They
have no Calamus official identity, no RetroMCP resolution, and no current
Feather identity. They are formal non-claims, not guessed mappings. Their
content-addressed set is versioned in
`mappings/b1.7.3/sem-m13-retractions.properties` and is reconstructed from the
excluded section of the exact SEM-M13 report under the official-runtime gate.

## Diagnostic queue

The SEM-M7 qualification queue remains a research and audit surface. Its
bytecode gaps, constructors, initializers, library members, and unresolved
aliases do not redefine the maintained mapping graph and do not block a
release. `worldline mappings promote` retains the optional
`bytecode-exhaustive` mode for teams that deliberately require an empty queue
and complete official-bytecode naming. That stronger diagnostic is not named
`complete-game` and is not a Worldline release criterion.

## Change rule

A future mapping program may add a graph identity only with the same
independent-source evidence used by SEM-M13. It may restore a retracted
identity only by replacing its explicit retraction with qualifying evidence.
Neither a new milestone nor narrative text can mutate an already sealed batch.
