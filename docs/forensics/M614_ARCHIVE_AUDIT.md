# M614 archive audit

Date: 2026-08-24

The Fable 2 ledger previously claimed that an independent, uncommitted M614
TNT quasi-connectivity worktree remained preserved. A bounded forensic audit
found no such payload.

The surviving worktree and branch both point to
`fe416a17023263b029b3368b7436e1f2b5932ffb`. Its reflog contains only the
branch-creation entry at that same commit, its Git status is clean, and the
lifecycle audit classifies it as a husk. No M614 or TNT-quasi path or ref was
present in any of the 12 archived bundles or either archived ZIP/tar pack.

The broad pre-cleanup artifacts checked were:

- `worldline-all-local-refs-before-cleanup-2026-08-23.bundle` — SHA-256
  `6983c78b90412f625eae5d206de04a00a984c066025a23ccf0f2c9437b9ab1ce`;
- `worldline-all-refs-before-branch-cleanup-2026-08-23.bundle` — SHA-256
  `0ced9d7c49b28b4a77a58d6ab138b27e998987bd581e9bc65c09f404933a7cd6`;
- `worldline-non-git-artifacts-before-cleanup-2026-08-23.tar.gz` — SHA-256
  `3f74ac4fba67f9a8864e5f439ec0132b7c84347aac13b7d68f2c3966812eb0fc`;
- `worldline-remote-branches-before-consolidation-2026-08-23.bundle` — SHA-256
  `e0795443ace848fe5d35f56f087ae614df002a34e11c74b4b6f06f296136f559`.

Conclusion: the prior preservation claim was a ledger fossil. If uncommitted
M614 implementation material ever existed, it is not recoverable from the
available archives. The public TNT quasi-connectivity identity remains
formally retracted; M552 is the retained TNT-related evidence boundary.
