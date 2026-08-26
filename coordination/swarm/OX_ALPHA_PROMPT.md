# Ox Alpha supervised milestone worker contract

The supervisor launches OpenCode only through `OxAlphaLauncher`. The launcher places the worker
message before every variadic `-f` attachment, fixes the reviewed model and effort variant, denies
nested task delegation in the executable agent permission map, and writes immutable stdout, stderr,
session, exit, base, and HEAD evidence for every attempt. A direct or ad hoc `opencode run` command
cannot establish a supervised worker receipt.
The launcher must close the child stdin immediately after process creation. In non-interactive run
mode, an open parent pipe prevents OpenCode from observing EOF and can leave every parallel worker
alive without creating a session or producing an edit. The executable self-test must prove EOF.
Every launch must bind `--control-base` to the exact orchestrator SHA that authorized the executable
checks. Both the milestone base and current worktree HEAD must contain that commit; otherwise stop
before OpenCode starts rather than mixing current readiness with an older Candidate Gate.
When controls advance after a retryable attempt, preserve the exact archive and use
`OxAlphaControlMigration` on the same branch, worktree, and session. Reapply the checkpoint only
after it verifies archive SHA-256, ancestry, CSM context, and both applicable scars on the new base.
If the primary provider reports a usage limit, classify and archive the attempt before setting
`WORLDLINE_OX_ALPHA_FALLBACK=1`. The allowlisted free fallback may resume only the same receipt-bound
session and dirty worktree on a later attempt; it may not open a replacement milestone or session.
A fallback checkpoint resume must receive at least 7200 seconds because a large receipt-bound
history can make correct progress beyond the primary one-hour budget. The launcher rejects a
shorter fallback retry and extends its own outer timeout beyond the worker budget.
If the selected free provider returns a systemic transport error, archive and classify the attempt
before setting `WORLDLINE_OX_ALPHA_FALLBACK_MODEL` to another reviewed free model. The executable
allowlist rejects arbitrary providers, and the same receipt-bound session, worktree, and milestone
ID remain mandatory.
These worktrees run on Windows PowerShell. Use native PowerShell or portable commands; do not spend
an inference step retrying Unix-only options such as `ls -la`, `find`, or `xargs`.

The supervisor must run `SwarmLoop preflight` with the exact immutable wave closure and supervised
micro-wave receipt before the worker edits or scaffolds anything. The worker ID must be listed in
that receipt, and total top-level Candidate workers must not exceed its adaptive width.
Only the canonical immutable receipt derived from that closure SHA-256 is valid; an alternate path or
a second receipt for the same learning barrier must fail before any worker edits.
The worktree must be exclusive, clean, and checked out at the exact authorized base SHA. The
preflight reads `AGENTS.md` and `docs/ENGINEERING_WORKFLOW.md` completely, runs CSM context, and
runs NYA recall for the milestone, smoke tooling, and TestKit paths. Recall must succeed and show
`NYA-01M0VSCA8F3WSMVW32R9XME7DQ`. If the recalled scope includes protocol or runtime smoke
boundaries, it must also show `NYA-01M0WZ04QQJ4T0KDN3V9FJC5GV` before work begins.
`csm context` may return one only when its stderr contains exclusively the recognized uninitialized
optional WTW/RTW/NWC stores and its NYA output contains the mandatory supervision scar. NYA recall
still must return zero; any other context or recall failure blocks the worker.
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
Every milestone with `MAP.md` must recall `NYA-01M0YSJXNA3TK6FHQW4QJ5RJZ5`; the objective
pre-Candidate interlock must compare its literal text with `expected.signal` from the same
`smoke.properties`, even when the map already contains the frozen trace and signature.
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
It locates a control body only after the matching balanced condition parenthesis; do not rewrite a
valid braced control merely because its condition contains nested calls.
Before invoking that interlock, enumerate every added smoke-source line that contains a complete
constructor, method, or control body and expand it to multiple physical lines. Zero matches is a
mandatory worker checkpoint; an interlock failure is a `RETRYABLE` recurrence that must be
archived and appended once to `NYA-01M0X81N6TG6TQ4RM02X6PH7R7` before correction.
Keep every new Java line at or below 120 columns so a candidate cannot increase the global
`*.long.files` or `*.long.lines` ratchets after isolated qualification.
Before Candidate Gate, the supervisor must run `SwarmLoop pre-candidate` for the exact milestone,
authorized base, and goal. Its objective readiness report must stay bound to the unchanged source
manifest. The interlock must compile the exact runner, adapter, module, and smoke source closure
with the Candidate compiler, including checked-exception compatibility, before emitting PASS.
It also runs the Candidate smoke-statement ceiling before freezing the manifest, validates every
six-column symbol row against the exact hashed `mappings.tiny`, and rejects any source above its
150-statement budget. A new supervised `DataDrivenCycle` must declare `expected.trace`; that exact
trace must be present in `MAP.md`, hash to `expected.signature`, and bind the runtime signature. A
flowing-water freeze fixture must install its moving-water control without notifying neighbors
before it proves the initial still/moving pair.
Candidate Gate fails closed when the report is missing or any file changed afterward.
Every candidate with smoke sources must recall `NYA-01M0YH9M17ETMZA0F5X7981K4P`; textual review
of imports or method signatures never substitutes for that exact compilation.
Every candidate that changes `modules/api` must recall `NYA-01M0YRVA4DD24Y22AHJQP2X3MF` and
compile the module at the release declared in `harness.properties`. The API defaults to Java 8:
use Java 8 value classes with explicit equality there, even when TestKit may use Java 21 records.
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

After each worker, the supervisor must classify the outcome and preserve its exact commit, patch,
logs, receipt, and evidence archive. At the micro-wave barrier, aggregate equivalent causes, record
or update each NYA scar exactly once, run `csm nya check`, and update this base prompt when the
correction creates a new applicable scar. Run `SwarmLoop close-wave` against the prior and current
censuses, then open one `SwarmMicroWave` receipt. Release no worker outside that receipt and release
no subsequent micro-wave until the same scar is proven absent. Candidate phases may run concurrently
up to the measured receipt width; official runtime remains serialized by the canonical lease.
Wave closure must recall `NYA-01M0Z06TZVCVMW4KR36YEE0ARY` and parse the archived pretty-printed
25-candidate census fixture; a zero-row or partial parse blocks the micro-wave.
Before either preflight or pre-Candidate, `RejectedContractCheck` must reject any ID or goal that is
semantically equivalent to `coordination/swarm/rejection-registry.properties`, including the
M674/M660 minecart-collision-transfer repetition. Only the same milestone ID may be revalidated,
and only after an objective change SHA is recorded.
This boundary must recall `NYA-01M0YZVBKBPB0SB3CJYVQSPNA9`; a changed milestone number never
creates a new semantic contract or resets the archived rejection.
Before the lifecycle audit that releases the next candidate, recall
`NYA-01M0YYN1QGEJ2G0DN9J8ZTBZT3` and run `java
tools/integration/WorktreeLifecycleLauncher.java audit --base <integrated-ref>`. The launcher must
compile the exact sibling-source closure before audit; launching `WorktreeLifecycle.java` directly
is fail-closed and cannot establish a clean cohort.
Before train pin migration, recall `NYA-01M0SX8SQGCT8RCH6KVDZH5DZC` and
`NYA-01M0YWM2GC786PNQD0WD1D6Z8T`; require `git status --porcelain
--untracked-files=all` to be empty so every new source is present in the committed diff. When a clean qualified
worktree supplies a complete current receipt, attestation, and log, import that exact execution
before considering predecessor or historical evidence and preserve `source=executed`. Before
migration, census local receipt/attestation/log tuples for every path the importer may treat as
an exact execution, not only the first reported failure. Preserve partial tuples in the audit
archive and treat them as absent; they must not block a complete worktree import, a complete
local execution, or complete predecessor fallback. Preserve source indexes from the immediate
predecessor lock, never `HEAD^`, and append newly tracked paths after the stable predecessor
order. For every source whose current digest equals the predecessor current digest, require its
ancestor count and ordered ancestor digests to remain byte-for-byte unchanged; the migration
self-test must fail before writing locks if an unchanged block advances. When a reviewed shared
TestKit input changes multiple fingerprints, carry unchanged
observations only through tracked `refactor-equivalent` proof envelopes.
Before `IntegrationTrain --reconcile`, recall `NYA-01M0YVZNPXM8D1JRQ4HWDK0X0J`, resolve the
authorized base and exact train ref, and require `git rev-list --count <base>..<ref>` to print
exactly `1`. If it does not, preserve the existing branch and build a new `codex/train-*`
consolidation ref; do not invoke the integration Gate or runtime on the multi-commit ref.
Before freezing that consolidated ref, recall `NYA-01M0YW91JWSC5SXCQG2HPH8APN`, run
`java tools/harness/Gate.java --refresh-readme-status` and `java tools/harness/Gate.java
--refresh-documentation`, then require the generated README status and documentation catalog
checks to pass. These global release surfaces belong to the train, never to a milestone worker.
When reconciliation changes TestKit sources, recall `NYA-01M0T6W6ECYZ8TG3XESYC9J5SV` and
on the exact clean committed train run `java tools/harness/Gate.java
--refresh-testkit-artifact-pins`. That maintenance command must compile and materialize the exact
module outputs, resolve immutable cache links, package TestKit, and validate every JAR entry while
writing `release/testkit-artifacts.lock`. Commit that exact lock before train pin migration and
the canonical integration Gate; never pin artifacts produced from predecessor cache links.

Only `QUALIFIED`, `RETRYABLE`, or `REJECTED` may survive supervisor classification. A worker that
exits without a disposition is `STRANDED` and must be converted immediately. `QUALIFIED` requires
a real public contract, no `scaffold.status`, one logical commit, a clean tree, candidate and
milestone Gate success, an exact HEAD/tree/base receipt, and a portable handoff. `REJECTED` requires
exact oracle, fixture, or instability evidence and an applicable NYA scar. `RETRYABLE` resumes the
same session and worktree under a bounded attempt count; it never opens an equivalent milestone.

No new micro-wave may start while a census contains dirty or failed workers, an unresolved disposition,
a rejection without a scar, a missing recall proof, a handoff without an exact receipt, or a draft
scaffold offered as a contract. No subsequent 25-candidate wave may start until every contract in
the current 25-candidate wave is qualified and integrated.
