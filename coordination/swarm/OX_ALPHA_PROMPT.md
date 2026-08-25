# Ox Alpha supervised milestone worker contract

The supervisor must run `SwarmLoop preflight` before the worker edits or scaffolds anything.
The worktree must be exclusive, clean, and checked out at the exact authorized base SHA. The
preflight reads `AGENTS.md` and `docs/ENGINEERING_WORKFLOW.md` completely, runs CSM context, and
runs NYA recall for the milestone, smoke tooling, and TestKit paths. Recall must succeed and show
`NYA-01M0VSCA8F3WSMVW32R9XME7DQ`.

Nested task/explore/subagent delegation is forbidden because this launcher does not supervise
nested work. Inspect the repository directly. A draft scaffold is fail-closed and is never a
contract, completion, handoff, or train candidate.

After each worker, the supervisor must classify the outcome, preserve its exact commit, patch,
logs, receipt, and evidence archive, then extract one reusable cause. Record or update NYA exactly
once, run `csm nya check`, and update this base prompt when the correction creates a new applicable
scar. Release the next candidate only after confirming that the same scar did not recur.

Only `QUALIFIED`, `RETRYABLE`, or `REJECTED` may survive supervisor classification. A worker that
exits without a disposition is `STRANDED` and must be converted immediately. `QUALIFIED` requires
a real public contract, no `scaffold.status`, one logical commit, a clean tree, candidate and
milestone Gate success, an exact HEAD/tree/base receipt, and a portable handoff. `REJECTED` requires
exact oracle, fixture, or instability evidence and an applicable NYA scar. `RETRYABLE` resumes the
same session and worktree under a bounded attempt count; it never opens an equivalent milestone.

No new wave may start while a census contains dirty or failed workers, an unresolved disposition,
a rejection without a scar, a missing recall proof, a handoff without an exact receipt, or a draft
scaffold offered as a contract.
