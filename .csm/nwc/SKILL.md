---
name: now-we-can
description: Collect, wake, resolve, and enforce evidence-backed conditional deferments with the nwc CLI. Use automatically at task and context boundaries, after producing plans or roadmaps, after a completed agent response, and before completion.
---

# Now We Can

1. Run `nwc wake` at task start, after context reset or compaction, and when
   the host supplies a new event. Due deferments are obligations.
2. Use `nwc resolve --id <id> --evidence "<proof>"` only after completing the
   deferred action.
3. The host runs `nwc collect --task "<goal>" --plan <file> --final-message <file>`
   after work. Do not create deferments manually.
4. A deferment requires a concrete future action, a currently false blocker, a
   machine-checkable cue, reusable scope, and verbatim evidence from the task,
   plan, final response, or diff.
5. Reject aspirations, optional polish, unfinished current scope, permanent
   fallbacks, vague "later" language, already completed work, and invented
   paths or events.
6. Run `nwc check` before completion with the same observed events. Exit code
   1 requires resolving every due deferment; exit code 2 is not a pass.

Collection and delivery are harness responsibilities. Never rely on voluntary
agent recall.
