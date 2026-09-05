# Aero experimental evidence

These are preserved experimental sources, not active or passing qualification
milestones. No failed acquisition is transported into a positive smoke pin.

- M791: vanilla-query occlusion rejected; zero useful culls with the corrected oracle.
  Exact source: c796c870dc32636774b155526e5ddbf93bba954e.
- M793: production qualification rejected by its exact oracle; the immutable
  baseline also differs at the first chunk traversal. Raw evidence and the
  complete disposition are documented in docs/AERO_PRODUCTION_DISPOSITION.md.
  Exact source: 3a9fe325bab35c1a2b67bd4534e09f579236561c.

To reproduce, create a clean worktree at the original source commit and run its
documented Gate --candidate and Gate --milestone commands. Its original paths,
contracts and thresholds are preserved here for review. Moving an experiment
out of the active catalog is a rejection, never a qualification or waiver.

The archived M793 digest adapter delegates to the now-public shared harness
helper for compatibility with this main. The original commit retains its exact
runner-local implementation; hashes, fixture and acceptance limits are unchanged.
