# Evidence-to-rule promotion

Promote a Worldline finding into the anti-slop ruleset only when all gates pass:

1. Reproduce the failure or excessive work on a pinned runtime.
2. Minimize the scenario without removing the behavior.
3. Identify the causal source or bytecode pattern.
4. State the invariant that should hold across projects.
5. Decide whether the invariant is statically decidable, bytecode-decidable, runtime-only, or mixed.
6. Add an invalid fixture that fails for the intended reason.
7. Add a nearby valid fixture that passes.
8. Emit a stable rule ID and an actionable diagnostic.
9. Run the rule against integrated projects and investigate every false positive.
10. Preserve the original Worldline scenario as runtime evidence when static analysis is insufficient.

Do not promote timing noise, stylistic preference, a single library's internal architecture, or an unproven decompiler interpretation. Prefer the narrowest rule that blocks the demonstrated failure without forbidding legitimate designs.

Record rule provenance as Worldline scenario or issue IDs and behavioral evidence. External projects retain ownership of their project-specific optimization catalogs; the generic ruleset may reference IDs but must not copy private implementation details.
