---
name: right-this-way
description: Find, follow, record, and verify repository-specific implementation patterns with the rtw CLI. Use at task start, after scope or context changes, when creating code analogous to existing features, after establishing a reusable reference implementation, and before commit, pull request, push, review, or completion.
---

# Right This Way

1. At task start, run `rtw guide --task "<goal>" --path <expected-path>` before editing. Read every returned way and inspect each referenced file.
2. Rerun `rtw guide` when scope changes, context is reset or compacted, an unfamiliar area is entered, or review begins.
3. Preserve the invariants and structure described by relevant ways. Adapt names and domain details to the current task. Never copy a reference mechanically.
4. Run `rtw add` only after a reusable pattern exists in tracked repository code. Provide a precise intent, actionable guidance, reusable scopes and tags, and at least one reference.
5. Before committing an uncommitted final diff, run `rtw check --task "<completed task>"`.
6. For committed review, pull-request preparation, or pre-push review, run `rtw check --base <target-revision> --task "<review context>"`.
7. Rerun the applicable check after every change to the reviewed diff. Exit code 1 requires alignment and another check. Exit code 2 means the audit did not complete and must never be reported as a pass.

Do not turn preferences, experiments, one-off code, or hypothetical designs into
ways. A way must point to a proven repository-local implementation.

Do not report work ready until the applicable `rtw check` exits with code 0.
