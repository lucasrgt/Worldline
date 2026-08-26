# Ox Alpha supervised milestone worker contract

The supervisor must run `SwarmLoop preflight` before the worker edits or scaffolds anything.
The worktree must be exclusive, clean, and checked out at the exact authorized base SHA. The
preflight reads `AGENTS.md` and `docs/ENGINEERING_WORKFLOW.md` completely, runs CSM context, and
runs NYA recall for the milestone, smoke tooling, and TestKit paths. Recall must succeed and show
`NYA-01M0VSCA8F3WSMVW32R9XME7DQ`. If the recalled scope includes protocol or runtime smoke
boundaries, it must also show `NYA-01M0WZ04QQJ4T0KDN3V9FJC5GV` before work begins.
When a milestone changes narrative fields in `smoke.properties`, recall must also present
`NYA-01M0X81N6TG6TQ4RM02X6PH7R7`; regenerate the canonical milestone narrative with
`MilestoneNarrative` before Candidate Gate instead of hand-authoring the generated document.
When a milestone adds or changes TestKit descriptor tokens, recall must also present
`NYA-01M0XFV9TPVDKFE6RARDHC84T2`; fixture, action, and observation tokens must be unique
lowercase-hyphen identifiers of at most 63 characters before Candidate Gate or runtime.
When a milestone adds or changes `symbols.map`, recall must also present
`NYA-01M0XM730NWRQDKFZ1VMP3732W`. Before Candidate Gate, compare every six-column symbol row
with the exact frozen `mappings.tiny` owner and method row. Treat client and server mapped names
as independent fields, preserve an empty side as an empty tab field, and never copy the populated
side into the empty side. The same descriptor audit must verify that `MAP.md` contains the exact
frozen semantic signal and that every `atlas.subsystems` value belongs to `AtlasSubsystems.ALL`.
Before Candidate Gate for a data-driven cycle, recall `NYA-01M0XWB16KZB3JRYDGAAYF5SVB` and
verify source closure from `cycle.inputs` and `cycle.compile.products`; never import a private
helper owned by another smoke when a maintained shared protocol surface supplies the behavior.
When a protocol fixture activates an interactive block after loading or moving inventory, recall
`NYA-01M0Y8SVKQRHV9JY1DX029BN60`. Before Candidate Gate, enumerate every activation call and
require an immediately preceding bounded proof that the selected hotbar slot is empty; a requested
slot change without an authoritative observed selection is not sufficient. For furnace restart
progress, recall `NYA-01M0Y96GN4DJTBWMYC36KDXHNA`: recovered inventory alone is a non-proof, and
qualification requires a bounded post-restart Packet105 snapshot with nonzero cook and burn.
Before the integration Gate, recall `NYA-01M0XRE7GSKH7ARKM73DVCGQ7K`, classify each newly added
smoke with `SmokeLane`, and increment the canonical total plus exactly one lane count in
`quality/smoke-lanes.properties`; leave every unrelated lane count unchanged.
Before a Gate path launches SmokeSuite or runtime mapping batches in a child JVM, recall
`NYA-01M0WVNTF94T6YNYNGTSABWTE9` and mark the parent qualification boundary. Executed child
qualification is cold work and must never be judged against the hot no-work SLO.
Treat any nonzero packed-line count reported by Candidate Gate as fail-closed even if that tier
passes; split the statements before committing so the canonical zero-growth ratchet cannot recur.
The objective interlock must also reject a constructor, method, or control body that opens and
closes on one physical line, including an empty constructor, before Candidate Gate.
Keep every new Java line at or below 120 columns so a candidate cannot increase the global
`*.long.files` or `*.long.lines` ratchets after isolated qualification.
Before Candidate Gate, the supervisor must run `SwarmLoop pre-candidate` for the exact milestone,
authorized base, and goal. Its objective readiness report must stay bound to the unchanged source
manifest. The interlock must compile the exact runner, adapter, module, and smoke source closure
with the Candidate compiler, including checked-exception compatibility, before emitting PASS.
Candidate Gate fails closed when the report is missing or any file changed afterward.
Every candidate with smoke sources must recall `NYA-01M0YH9M17ETMZA0F5X7981K4P`; textual review
of imports or method signatures never substitutes for that exact compilation.
When milestone work adds a recursive harness or orchestration scan, recall
`NYA-01M0XYP7T1RKYFD3SJHC4DMHZ3` and use the reviewed no-follow traversal helper.
For portal cooldown or portal re-entry contracts, recall must also present
`NYA-01M0XC2P1Y3MCKVAHJEAJXX16C`; prove suppression during continuous arrival-portal contact and
pair it with a positive exit-and-reentry control from the same player.
For minecart-collision contracts, recall must also present
`NYA-01M0YCEZH1G2SKW1DVB1D4K3SB`, `NYA-01M0YDKWFZ4H1CCXE2TXCJC31G`,
`NYA-01M0YM0FGRMPQ4DABMTVS4MNAF`, and `NYA-01M0YMWRZX8V20G1SN0DYGB0MD`.
Stone supports are fixture geometry, not collision evidence: preprovision them deterministically
instead of calling `B173FixtureSupport.place(..., 1)`. Observe required rail and support states
before extending the fixture or starting the collision oracle. A Packet7 attack proven only by
the parallel-rail booster contract is not a same-rail mover-initiation primitive. Readiness rejects
`B173MinecartBooster.push` in minecart-collision candidates; use a separately qualified bounded
mover-initiation mechanism before attempting collision-transfer evidence.

Nested task/explore/subagent delegation is forbidden because this launcher does not supervise
nested work. Inspect the repository directly. A draft scaffold is fail-closed and is never a
contract, completion, handoff, or train candidate.

After each worker, the supervisor must classify the outcome, preserve its exact commit, patch,
logs, receipt, and evidence archive, then extract one reusable cause. Record or update NYA exactly
once, run `csm nya check`, and update this base prompt when the correction creates a new applicable
scar. Release the next candidate only after confirming that the same scar did not recur.
Before train pin migration, recall `NYA-01M0SX8SQGCT8RCH6KVDZH5DZC`; when a clean qualified
worktree supplies a complete current receipt, attestation, and log, import that exact execution
before considering predecessor or historical evidence and preserve `source=executed`. Before
migration, census local receipt/attestation/log tuples for every path the importer may treat as
an exact execution, not only the first reported failure. Preserve partial tuples in the audit
archive and treat them as absent; they must not block a complete worktree import, a complete
local execution, or complete predecessor fallback. Preserve source indexes from the immediate
predecessor lock, never `HEAD^`, and append newly tracked paths after the stable predecessor
order. When a reviewed shared TestKit input changes multiple fingerprints, carry unchanged
observations only through tracked `refactor-equivalent` proof envelopes.
When reconciliation changes TestKit sources, recall `NYA-01M0T6W6ECYZ8TG3XESYC9J5SV` and
refresh the reconciled module outputs with the applicable static Candidate Gate before packaging.
Write `release/testkit-artifacts.lock` only from those exact refreshed artifacts, then run the
canonical integration Gate; never pin artifacts produced from predecessor cache links.

Only `QUALIFIED`, `RETRYABLE`, or `REJECTED` may survive supervisor classification. A worker that
exits without a disposition is `STRANDED` and must be converted immediately. `QUALIFIED` requires
a real public contract, no `scaffold.status`, one logical commit, a clean tree, candidate and
milestone Gate success, an exact HEAD/tree/base receipt, and a portable handoff. `REJECTED` requires
exact oracle, fixture, or instability evidence and an applicable NYA scar. `RETRYABLE` resumes the
same session and worktree under a bounded attempt count; it never opens an equivalent milestone.

No new wave may start while a census contains dirty or failed workers, an unresolved disposition,
a rejection without a scar, a missing recall proof, a handoff without an exact receipt, or a draft
scaffold offered as a contract.
