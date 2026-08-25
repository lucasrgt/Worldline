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
Treat any nonzero packed-line count reported by Candidate Gate as fail-closed even if that tier
passes; split the statements before committing so the canonical zero-growth ratchet cannot recur.
Keep every new Java line at or below 120 columns so a candidate cannot increase the global
`*.long.files` or `*.long.lines` ratchets after isolated qualification.
For portal cooldown or portal re-entry contracts, recall must also present
`NYA-01M0XC2P1Y3MCKVAHJEAJXX16C`; prove suppression during continuous arrival-portal contact and
pair it with a positive exit-and-reentry control from the same player.

Nested task/explore/subagent delegation is forbidden because this launcher does not supervise
nested work. Inspect the repository directly. A draft scaffold is fail-closed and is never a
contract, completion, handoff, or train candidate.

After each worker, the supervisor must classify the outcome, preserve its exact commit, patch,
logs, receipt, and evidence archive, then extract one reusable cause. Record or update NYA exactly
once, run `csm nya check`, and update this base prompt when the correction creates a new applicable
scar. Release the next candidate only after confirming that the same scar did not recur.
Before train pin migration, recall `NYA-01M0SX8SQGCT8RCH6KVDZH5DZC`; when a clean qualified
worktree supplies a complete current receipt, attestation, and log, import that exact execution
before considering predecessor or historical evidence and preserve `source=executed`.

Only `QUALIFIED`, `RETRYABLE`, or `REJECTED` may survive supervisor classification. A worker that
exits without a disposition is `STRANDED` and must be converted immediately. `QUALIFIED` requires
a real public contract, no `scaffold.status`, one logical commit, a clean tree, candidate and
milestone Gate success, an exact HEAD/tree/base receipt, and a portable handoff. `REJECTED` requires
exact oracle, fixture, or instability evidence and an applicable NYA scar. `RETRYABLE` resumes the
same session and worktree under a bounded attempt count; it never opens an equivalent milestone.

No new wave may start while a census contains dirty or failed workers, an unresolved disposition,
a rejection without a scar, a missing recall proof, a handoff without an exact receipt, or a draft
scaffold offered as a contract.
