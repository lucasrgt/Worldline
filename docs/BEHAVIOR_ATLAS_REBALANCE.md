# Behavior Atlas Placement Rebalance

The former `block-placement-persistence` bucket grouped 109 distinct official
server scenarios under one public behavior identity. The reviewed taxonomy now
keeps 15 ordinary structural cases in that contract and assigns the other 94
to six function-specific identities:

| Behavior | Milestones |
| --- | ---: |
| `decorative-block-placement` | 34 |
| `resource-block-placement` | 15 |
| `oriented-block-placement` | 13 |
| `component-placement` | 13 |
| `vegetation-placement` | 12 |
| `utility-block-placement` | 7 |

`BehaviorFamilyAssignments` is the closed 109-entry classification. The Gate
requires every assigned descriptor to carry its exact public token and verifies
that the old and new descriptor hashes are preserved in
`smokes/behavior-family-rebalance.lock`.

This is a semantic-taxonomy migration, not new vanilla evidence. The executable
runner, fixture, actions, observations, frozen signal, and frozen signature are
unchanged. Existing official observations are transported through content-
addressed evidence envelopes; five already-pending TestKit/GUI observations
remain pending and M620 remains unpinned.

Three previously orphaned public tokens are intentionally retained rather than
silently deleted: `tnt-quasi-connectivity`, `one-tick-piston-pulse`, and
`hostile-spawn-light`. The M614, M615, and M617 milestone lines are responsible
for requalifying them. COV-02 remains active until all three exact milestone
gates are integrated and current.
